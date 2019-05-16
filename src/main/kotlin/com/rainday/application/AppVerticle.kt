package com.rainday.application

import com.rainday.`val`.DEFAULT_USERAGENT
import com.rainday.`val`.ROUTE_INFO
import com.rainday.dto.RelayDto
import com.rainday.handler.Global404Handler
import com.rainday.handler.Global413Handler
import com.rainday.model.UrlTemplate
import com.rainday.model.getParameter
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
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
        router.get("/routes").handler(this::listRoutes)
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
                router.route(HttpMethod.valueOf(relayDto.inMethod), relayDto.inUrl).handler(this::relayHttpClient)
                vertx.sharedData().getAsyncMap<String, Any>(ROUTE_INFO) {
                    it.result().put(relayDto.inUrl, relayDto) {}
                }
            }
        }
    }
    
    override fun stop() {
        super.stop()
    }

    /* 返回当前所有的route */
    fun listRoutes(rc: RoutingContext) {
        vertx.sharedData().getAsyncMap<String, Any>(ROUTE_INFO){
            it.result().values {
                rc.response().end(Json.encodePrettily(it.result()))
            }
        }
    }

    fun relayHttpClient(rc: RoutingContext) {

        val path = rc.normalisedPath()
        val f1 = Future.future<AsyncMap<String, Any>>()

        vertx.sharedData().getAsyncMap<String, Any>(ROUTE_INFO, f1)
        f1.compose {
            Future.future<Any>().apply {
                it.get(path, this)
            }
            /*it.get(path,f2)
            f2*/
        }.setHandler {
            if (it.result() == null) {
                rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                    .setStatusMessage(HttpResponseStatus.NOT_FOUND.reasonPhrase())
                    .end("resources-($path) not found ")
            } else {
                val relayDto = it.result() as RelayDto
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
        /*var toPath = rc.currentRoute().toPath(routeExtInfoMap)
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
        }*/
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
    fun registerAppConsumers(vertx: Vertx, deployId:String) {
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

