package com.rainday.application

import com.rainday.`val`.DEFAULT_USERAGENT
import com.rainday.`val`.ROUTE_INFO
import com.rainday.dto.RelayDto
import com.rainday.exception.TagwException
import com.rainday.handler.Global404Handler
import com.rainday.handler.Global413Handler
import com.rainday.handler.queryRoutes
import com.rainday.model.Code
import com.rainday.model.UrlTemplate
import com.rainday.model.getParameter
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.streams.Pump
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

/**
 * Created by wyd on 2019/3/1 10:16:53.
 */
class AppVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val defaultTime = 10
    private val router by lazy { Router.router(vertx) }
    private val httpclient by lazy {
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
        //启动本app+

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("appPort"))
    }

    override fun start() {
        super.start()
        val relays = config().getJsonArray("relays")
        //生成route，并将route对应的转发信息保存在asyncMap中
        relays?.map(JsonObject::mapFrom)?.forEach {
            val relayDto = it?.mapTo(RelayDto::class.java)
            relayDto?.also {
                Future.future<AsyncMap<String, Any>>().apply {
                    vertx.sharedData().getAsyncMap(ROUTE_INFO, this)
                }.compose { map ->
                    Future.future<Any>().apply {
                        map.get(relayDto.inUrl, this)
                    }.compose {
                        if (it == null) {
                            Future.future<Void>().apply {
                                println("增加route ${relayDto.inUrl}")
                                router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl)
                                    .handler(::relayHttpClient)
                                map.put(relayDto.inUrl, relayDto, this)
                            }.compose {
                                Future.succeededFuture("route 设置成功")
                            }
                        } else {
                            Future.succeededFuture("route 已存在，无需重复设置")
                        }
                    }
                }.setHandler {
                    println(it.result())
                }
                /*//callback hell
                vertx.sharedData().getAsyncMap<String, Any>(ROUTE_INFO) { map ->
                    map.result().get(relayDto.inUrl) {
                        if (it.result() == null) {
                            map.result().put(relayDto.inUrl, relayDto) {
                                if (it.succeeded()) {
                                    println("增加route ${relayDto.inUrl}")
                                    router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl)
                                        .handler(::relayHttpClient)
                                }
                                *//*it.takeIf { it.succeeded() }?.run {
                                    router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl).handler(::relayHttpClient)
                                }*//*
                            }
                        }
                    }
                }*/
            }
        }
    }

    override fun stop() {
        super.stop()
    }

    fun relayHttpClient(rc: RoutingContext) {

        //rc.normalisedPath()  获取的是填充pathvariable之后的url
        val path = rc.currentRoute().path

        Future.future<AsyncMap<String, Any>>().apply {
            vertx.sharedData().getAsyncMap(ROUTE_INFO, this)
        }.compose {
            Future.future<Any>().apply {
                it.get(path, this)
            }
        }.compose {
            if (it == null) {
                Future.failedFuture(TagwException(Code.relay404))
            } else {
                Future.succeededFuture(it)
            }
        }.compose {
            //todo 异常处理。1、URL格式化异常。2、参数找不到异常。2.1参数位置不对等。
            //todo 增加对body参数的变形处理,可以使用handler编排机制。或者引入dsl。
            val relayDto = it as RelayDto
            //使用urltemplate处理参数
            val template = UrlTemplate(relayDto.outUrl)
            relayDto.parampairs?.forEach {
                template.setParam(it.outType, it.outName, rc.getParameter(it.inType, it.inName))
            }
            //创建httpclient，转发请求
            val clientRequest = httpclient.requestAbs(HttpMethod.valueOf(relayDto.outMethod), template.toString())
            Future.succeededFuture<Pair<RelayDto, HttpClientRequest>>(relayDto to clientRequest)
        }.setHandler {
            when (it.succeeded()) {
                false -> {
                    rc.response().setStatusCode(HttpResponseStatus.EXPECTATION_FAILED.code())
                        .setStatusMessage(HttpResponseStatus.EXPECTATION_FAILED.reasonPhrase())
                        .end("请求异常")
                    logger.error("relayHttpClient 异常，", it.cause())
                }

                true -> {
                    val relayDto = it.result().first
                    val clientRequest = it.result().second

                    clientRequest.headers().addAll(rc.request().headers())
                    if (relayDto.transmission.toInt() == 1) {
                        Pump.pump(rc.request(), clientRequest).start()
                    }
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
}

