package com.rainday.application

import com.rainday.`val`.APPLICATION_INFO
import com.rainday.`val`.DEFAULT_USERAGENT
import com.rainday.`val`.routeExtInfoMap
import com.rainday.model.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.streams.Pump
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

/**
 * Created by wyd on 2019/3/1 10:16:53.
 * 一个app应该具有的功能为 转发请求，动态修改路由
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
        addVerticleDeployInfo(vertx, context)
        registerAppConsumers(vertx, deploymentID())
        //初始化路由，管理本app 的route
        router.get("/routes").handler(this::listRoutes)
        //启动本app
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("port"))
    }

    override fun start() {
        super.start()
        val relays = config().getJsonArray("relays")
        relays?.map(JsonObject::mapFrom)?.forEach {
            val relay = it?.mapTo(Relay::class.java)
            if (relay != null) {
                val route = router.route(relay.inMethod, relay.inUrl.path).handler(this::relayHttpClient)
                routeExtInfoMap.put(route.hashCode(), relay)
            }
        }
    }
    
    override fun stop() {
        super.stop()
        deleteVerticleDeployInfo(vertx, this.deploymentID())
    }

    /* 返回当前所有的route */
    fun listRoutes(rc: RoutingContext) {
        //剔除掉系统信息的route
        val result = router.routes.map { routeExtInfoMap[it.hashCode()] }.filterNotNull()
        rc.response().end(Json.encodePrettily(result))
    }

    fun relayHttpClient(rc: RoutingContext) {
        println(this)
        var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val relay = rc.currentRoute().getBindInfo(routeExtInfoMap)

        //使用urltemplate处理参数
        val template = UrlTemplate(relay?.outUrl.toString())
        relay?.paramPairs?.forEach {
            template.setParam(it.outType, it.outName, rc.getParameter(it.inType, it.inName))
        }
        val clientRequest = httpclient.requestAbs(httpMethod, template.toString())
        clientRequest.headers().addAll(rc.request().headers())
//        clientRequest.putHeader("Content-Type",rc.request().getHeader("Content-Type"))
//        clientRequest.putHeader("Content-Length",rc.request().getHeader("Content-Length"))
        clientRequest.headers().addAll(template.headerParamMap)

        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers().entries())}")
        //将收到的数据打到后端服务器，可以认为是透传模式。透传-将inboundbody传个后端服务器
        //todo 后续添加参数变形模式
        Pump.pump(rc.request(), clientRequest).start()
        //请求结束
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

    fun relayWebClient(rc: RoutingContext) {
        var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val jsonObject = rc.currentRoute().getBindInfo(routeExtInfoMap)
        val clientRequest = webClient.requestAbs(httpMethod, toPath.toString())
        clientRequest.headers().addAll(rc.request().headers())
        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers().entries())}")
    }
    
    //注册APP management consumer
    fun registerAppConsumers(vertx: Vertx, deployId:String) {
        //更新APPname
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
        }
    }
}

