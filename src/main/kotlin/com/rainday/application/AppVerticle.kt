package com.rainday.application

import com.rainday.`val`.DEFAULT_USERAGENT
import com.rainday.`val`.FIND_RELAY_ALL
import com.rainday.dto.ApplicationDto
import com.rainday.ext.toJsonString
import com.rainday.handler.Global404Handler
import com.rainday.handler.Global413Handler
import com.rainday.model.UrlTemplate
import com.rainday.model.getParameter
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.streams.Pump
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.ext.web.handler.BodyHandler

/**
 * Created by wyd on 2019/3/1 10:16:53.
 */
class AppVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val defaultTime = 10
    private val appDto by lazy {
        if (logger.isDebugEnabled) {
            logger.debug("appVerticle-(${this.toString()})-config: ${config()} ")
        }
        config().mapTo(ApplicationDto::class.java).apply { this.deployId = deploymentID() }
    }
    private val relayMap by lazy {
        //list按照inUrl分组，转换成为map
        appDto.relays.associateBy { it.inUrl }
    }
    private val router by lazy { Router.router(vertx) }
    private val httpclient by lazy {
        if (logger.isDebugEnabled) {
            logger.debug("appVerticle-(${this.toString()})-httpclient: init")
        }
        vertx.createHttpClient(
            HttpClientOptions()
                .setIdleTimeout(defaultTime)
                .setKeepAliveTimeout(defaultTime * 2)
        )//时间单位默认 秒
    }

    private val webClient by lazy {
        WebClient.create(vertx, WebClientOptions().setKeepAliveTimeout(defaultTime).setUserAgent(DEFAULT_USERAGENT))
    }

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        registerAppConsumers(vertx, deploymentID())
        //errorHandler
        router.errorHandler(HttpResponseStatus.NOT_FOUND.code(), ::Global404Handler)
        router.errorHandler(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code(), ::Global413Handler)
        //初始化路由，管理本app 的route
        router.get("/routes").handler(::queryRoutes)
        router.get("/relays").handler(::queryRelays)
        router.get("/app").handler(::queryApp)
        //启动本app
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("appPort"))
    }

    override fun start() {
        super.start()
        //生成route
        relayMap.forEach { _, relayDto ->
            if (relayDto.transmission == 0.toShort()) {
                if (logger.isDebugEnabled) logger.debug("request set bodyhandler ")
                router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl)
                    .handler(BodyHandler.create())
                    .handler(::relayHttpClient)
            } else {
                router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl)
                    .handler(::relayHttpClient)
            }
        }
    }

    override fun stop() {
        super.stop()
    }

    fun relayHttpClient(rc: RoutingContext) {
        //rc.normalisedPath()  获取的是填充pathvariable之后的url
        val path = rc.currentRoute().path ?: ""
        when (relayMap.containsKey(path)) {
            true -> {
                val relayDto = relayMap[path]
                if (relayDto == null) {
                    rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                        .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                        .end("never execute")
                } else {
                    //使用urltemplate处理非 body参数
                    val template = UrlTemplate(relayDto.outUrl)
                    relayDto.parampairs?.forEach {
                        template.setParam(it.outType, it.outName, rc.getParameter(it.inType, it.inName))
                    }
                    //创建httpclient，转发请求
                    val clientRequest =
                        httpclient.requestAbs(HttpMethod.valueOf(relayDto.outMethod), template.toString())
                    //处理请求头
                    clientRequest.headers()
                        .setAll(rc.request().headers())
                        .setAll(template.headerParamMap)
                        .set("tagw", DEFAULT_USERAGENT)
                        .remove("content-length")

                    //后端结果handler,使用数据泵将数据打回用户浏览器
                    clientRequest.handler {
                        if (logger.isDebugEnabled) logger.debug("clientRequest send headers ${Json.encode(clientRequest.headers().entries())}")
                        if (logger.isDebugEnabled) logger.debug("clientRequest response header ${Json.encode(it.headers().entries())}")
                        //将后端服务响应header全部传回client
                        rc.response().headers().addAll(it.headers()).add("tagw", DEFAULT_USERAGENT)
                        Pump.pump(it, rc.response()).start()
                        it.endHandler {
                            if (logger.isDebugEnabled) logger.debug("clientRequest response endHandler --------- connection: ${clientRequest.connection()}")
                            rc.response().end()
                        }
                    }
                    //exception handler
                    clientRequest.exceptionHandler {
                        rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                            .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                            .end("请求异常${it.message} ${it.localizedMessage}")
                    }

                    //1:开启body透传，0:关闭body透传，使用body转型。
                    if (relayDto.transmission == 1.toShort()) {
                        //todo 因为添加了bodyhandler这里在用pump不知道会不会报错
                        Pump.pump(rc.request(), clientRequest).start()
                        //上游请求结束，则结束下游请求
                        rc.request().endHandler {
                            if (logger.isDebugEnabled) logger.debug("rc.request trigger endHandler 1 --------- connection: ${rc.request().connection()}")
                            clientRequest.end()
                        }
                        //todo 判断如果bodymap为empty，则直接end是否会提高性能
                    } else if (relayDto.transmission == 0.toShort()) {
                        if (logger.isDebugEnabled) logger.debug("rc.request trigger endHandler 2 --------- connection: ${rc.request().connection()}")
                        val body = Json.encode(template.bodyParamMap)
                        clientRequest
                            .putHeader("content-length",body.length.toString())
                            .end(body)
                    }
                }
            }
            else -> rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                .end("never execute")
        }
    }

    fun relayWebClient(rc: RoutingContext) {
        /*var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val jsonObject = rc.currentRoute().getBindInfo(routeExtInfoMap)
        val clientRequest = webClient.requestAbs(httpMethod, toPath.toString())
        clientRequest.headers().addAll(rc.request().headers())
        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers().entries())}")*/
    }

    //注册APP management consumer
    fun registerAppConsumers(vertx: Vertx, deployId: String) {
        /*//更新APPname
        vertx.eventBus().consumer<String>("${deployId}${Action.UPDATE_APPNAME}") {
            val appName = it.body()
            val app = vertx.globalGet(APPLICATION_INFO,deployId)
            app?.apply {
                this as JsonObject
                this.put("appName",appName)
                vertx.globalPut(APPLICATION_INFO,deployId,this)
            }
        }
        //更新APP description
        vertx.eventBus().consumer<String>("${deployId}${Action.UPDATE_APPDESCRIPTION}") {
            val description = it.body()
            val app = vertx.globalGet(APPLICATION_INFO,deployId)
            app?.apply {
                this as JsonObject
                this.put("description",description)
                vertx.globalPut(APPLICATION_INFO,deployId,this)
            }
        }*/
    }

    /**
     * 查询本verticle instance 中的 relays
     */
    fun queryRelays(rc: RoutingContext) {
        rc.response().end(Json.encodePrettily(relayMap))
    }

    /**
     * 查询本verticle instance中的 app
     */
    fun queryApp(rc: RoutingContext) {
        rc.response().end(Json.encodePrettily(appDto))
    }


    /**
     * 查询routeInfo
     * @param d 2：查询数据库；1：查询当前verticle；默认：查verticle
     */
    fun queryRoutes(rc: RoutingContext) {
        val d = rc.request().getParam("d") ?: ""
        when (d) {
            "2" -> rc.vertx().eventBus().send<String>(FIND_RELAY_ALL, d) {
                rc.response().end(it.result().body())
            }
            else -> rc.response().end(relayMap.toJsonString())
        }
    }
}

