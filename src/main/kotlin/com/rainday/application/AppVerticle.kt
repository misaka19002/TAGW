package com.rainday.application

import com.rainday.`val`.routeExtInfoMap
import com.rainday.model.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.Pump
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import java.net.URL
import java.util.stream.Collectors

/**
 * Created by wyd on 2019/3/1 10:16:53.
 * 一个app应该具有的功能为 转发请求，动态修改路由
 * 功能1 路由管理，动态crud
 * 功能2 请求转发(暂不考虑前置处理器，后置处理器)
 */
class AppVerticle : AbstractVerticle() {
    private val defaultTime = 10

    private val router by lazy { Router.router(vertx) }
    private val httpclient by lazy {
        vertx.createHttpClient(
            HttpClientOptions()
                .setIdleTimeout(defaultTime)
                .setKeepAliveTimeout(defaultTime)
        )//时间单位默认 秒
    }

    private val webClient by lazy {
        WebClient.create(vertx, WebClientOptions().setKeepAliveTimeout(defaultTime).setUserAgent("tagw/1.0.0"))
    }

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        //初始化路由，管理本app 的route
        router.get("/routes").handler(this::listRoutes)
        //启动本app
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("port"))
    }

    override fun start() {
        super.start()
        //根据参数设置 route。inUrl重复，不会生效此条route

        val relays = config().getJsonArray("relays")
        relays?.map(JsonObject::mapFrom)?.forEach {
            val relay = it?.mapTo(Relay::class.java)
            if (relay != null) {
                val route = router.route(relay.inMethod, relay.inUrl.path).handler(this::relayHttpClient)
                routeExtInfoMap.put(route.hashCode(), relay)
            }
        }
    }

    /* 返回当前所有的route */
    fun listRoutes(rc: RoutingContext) {
        //剔除掉系统信息的route
        val result = router.routes.map { routeExtInfoMap[it.hashCode()] }.filterNotNull()
        rc.response().end(Json.encodePrettily(result))
    }

    fun relayHttpClient(rc: RoutingContext) {
        var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val jsonObject = rc.currentRoute().getBindInfo(routeExtInfoMap)
        println(toPath.toString())
        URL("http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859")
        val clientRequest = httpclient.requestAbs(httpMethod, toPath.toString())
        clientRequest.headers().addAll(rc.request().headers().set("host", clientRequest.absoluteURI()))

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
    }

    fun relayWebClient(rc: RoutingContext) {
        var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val jsonObject = rc.currentRoute().getBindInfo(routeExtInfoMap)
        val clientRequest = webClient.requestAbs(httpMethod, toPath.toString())
        clientRequest.headers().addAll(rc.request().headers())
        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers().entries())}")

    }
}

