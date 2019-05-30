package com.rainday.application

import com.rainday.`val`.DEFAULT_USERAGENT
import com.rainday.dto.ApplicationDto
import com.rainday.handler.Global404Handler
import com.rainday.handler.Global413Handler
import com.rainday.handler.queryRoutes
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
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.core.streams.write

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
        //生成route，并将route对应的转发信息保存在asyncMap中
        relayMap?.forEach { inUrl, relayDto ->
            router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl).handler(::relayHttpClient)
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
                val relayDto = relayMap["path"]
                if (relayDto == null) {
                    rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                        .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                        .end("never execute")
                } else {
                    //使用urltemplate处理参数
                    val template = UrlTemplate(relayDto.outUrl)
                    relayDto.parampairs?.forEach {
                        template.setParam(it.outType, it.outName, rc.getParameter(it.inType, it.inName))
                    }
                    //创建httpclient，转发请求
                    val clientRequest = httpclient.requestAbs(HttpMethod.valueOf(relayDto.outMethod), template.toString())
                    clientRequest.headers().addAll(rc.request().headers())
                    if (relayDto.transmission.toInt() == 1) {
                        Pump.pump(rc.request(), clientRequest).start()
                    } else if (template.bodyParamMap.isNotEmpty()) {
                        clientRequest.write(false) {
                            this.obj(template.bodyParamMap)
                        }
                    }
                    mapOf<String, String>().forEach { t, u ->  }
                    //上游请求结束，则结束下游请求
                    rc.request().endHandler {
                        println("rc.request trigger endHandler --------- connection: ${rc.request().connection()}")
                        clientRequest.end()
                    }
                    //后端结果handler,使用数据泵将数据打回用户浏览器
                    clientRequest.handler {
                        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers().entries())}")
                        println("clientRequest handler 处理response ")
                        println("clientRequest response header ${Json.encode(it.headers().entries())}")
                        rc.response().headers().addAll(it.headers())
                        Pump.pump(it, rc.response()).start()
                        it.endHandler {
                            println("client.response trigger endHandler --------- connection: ${clientRequest.connection()}")
                            rc.response().end()
                        }
                    }

                    clientRequest.exceptionHandler {
                        rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                            .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                            .end("请求异常${it.message} ${it.localizedMessage}")
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
    fun aa(cc: String) = {
        cc.plus("sfsdf")
    }
}

