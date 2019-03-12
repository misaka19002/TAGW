package com.rainday.application

import com.rainday.model.bindInfo
import com.rainday.model.getBindInfo
import com.rainday.model.toMethod
import com.rainday.model.toPath
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.Pump
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

/**
 * Created by wyd on 2019/3/1 10:16:53.
 * 一个app应该具有的功能为 转发请求，动态修改路由
 * 功能1 路由管理，动态crud
 * 功能2 请求转发(暂不考虑前置处理器，后置处理器)
 */
class AppVerticle : AbstractVerticle() {
    private val defaultTime = 10
    private val routeExtInfoMap = HashMap<Int, JsonObject>()

    private val router by lazy { Router.router(vertx) }
    private val httpclient by lazy {
        vertx.createHttpClient(
            HttpClientOptions()
                .setIdleTimeout(defaultTime)
                .setKeepAliveTimeout(defaultTime)
        )//时间单位默认 秒
    }

    private val fake = JsonArray(
        "[\n" +
                "  {\n" +
                "    \"fromPath\": \"/jianshu/p/:pid\",\n" +
                "    \"fromMethod\": \"GET\",\n" +
                "    \"toPath\": \"https://www.jianshu.com/p/%s\",\n" +
                "    \"toMethod\": \"GET\",\n" +
                "    \"params\": [\n" +
                "      {\n" +
                "        \"fromName\": \"pid\",\n" +
                "        \"fromType\": \"path\",\n" +
                "        \"toName\": \"p1\",\n" +
                "        \"toType\": \"path\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"fromPath\": \"/jianshu1/p/:pid\",\n" +
                "    \"fromMethod\": \"GET\",\n" +
                "    \"toPath\": \"https://www.jianshu.com/p/%s\",\n" +
                "    \"toMethod\": \"GET\",\n" +
                "    \"params\": [\n" +
                "      {\n" +
                "        \"fromName\": \"pid\",\n" +
                "        \"fromType\": \"path\",\n" +
                "        \"toName\": \"p1\",\n" +
                "        \"toType\": \"path\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]"
    )

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        //初始化路由，管理本app 的route
        router.get("/routes").handler(this::listRoutes)

    }

    override fun start() {
        super.start()

        //造数据, 业务请求转发处理
        fake.map(JsonObject::mapFrom).forEach {
            //绑定handler
            this.router.route(it.getString("fromPath"))
                .method(HttpMethod.valueOf(it.getString("fromMethod")))
                .handler(this::relay)
                .failureHandler(null)
                .bindInfo(it, routeExtInfoMap)

            //将fromPath与头怕
        }

        //启动本app
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("port"))
    }

    fun listRoutes(rc: RoutingContext) {
        rc.response().end(JsonArray(router.routes).toString())
    }

    fun relay(rc: RoutingContext) {
        var toPath = rc.currentRoute().toPath(routeExtInfoMap)
        val httpMethod = rc.currentRoute().toMethod(routeExtInfoMap)
        val jsonObject = rc.currentRoute().getBindInfo(routeExtInfoMap)
        toPath = toPath.format(rc.pathParam("pid"))
        println(toPath)
        toPath = "https://www.jianshu.com/p/7fdd234cb52b"
        val clientRequest = httpclient.requestAbs(httpMethod, toPath)
        clientRequest.headers().addAll(rc.request().headers())
        println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers())}")

        //将收到的数据打到后端服务器，可以认为是透传模式
        //todo 后续添加参数变形模式
        Pump.pump(rc.request(), clientRequest).start()
        //请求结束
        rc.request().endHandler {
            println("client trigger endHandler")
            clientRequest.end()
        }

        //后端结果handler,使用数据泵将数据打回用户浏览器
        clientRequest.handler {
            println("clientRequest 发送请求时headers ${Json.encode(clientRequest.headers())}")
            println("clientRequest handler 处理response ")
            println("clientRequest 发送请求时headers ${Json.encode(it.headers())}")
            rc.response().headers().addAll(it.headers())
            Pump.pump(it, rc.response()).start()
        }
    }
}


