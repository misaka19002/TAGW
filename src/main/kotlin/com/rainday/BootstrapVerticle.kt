package com.rainday

import com.rainday.application.DataVerticle
import com.rainday.handler.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import javax.ws.rs.core.MediaType

/**
 * Created by wyd on 2019/2/28 17:10:23.
 * BootstrapVerticle(MainVerticle)
 * deploy/undeploy  create/delete/update/query apps
 * 管理APP的verticle,在项目启动时就deploy，默认占用80端口。
 */
class BootstrapVerticle : AbstractVerticle() {

    private val defaultPort = 8080
    private val router by lazy { Router.router(vertx) }

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
    }

    override fun start() {
        /* 部署application 管理 verticle */
        router.errorHandler(HttpResponseStatus.NOT_FOUND.code(), ::Global404Handler)
        router.errorHandler(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.code(), ::Global413Handler)
        router.route("/*").handler(BodyHandler.create())
        /* 查询routeinfo */
        router.get("/routes").handler(::queryRoutes)

        /* 查询可以有附加条件APP */
        router.get("/apps").handler(::queryApp)

        /* 部署新的APP */
        router.post("/apps").consumes(MediaType.APPLICATION_JSON).handler(::deployApp)

        /* 查询某个APP */
        router.get("/apps/:deployId").handler(::findApp)

        /* 更新某个APP - 辅助字段action存在在header中用来决定具体是哪种类型的更新 */
        router.put("/apps/:deployId").consumes(MediaType.APPLICATION_JSON).handler(::updateApp)

        /* 删除某个APP */
        router.delete("/apps/:deployId").handler(::deleteApp)

        /* 查看所有verticle */
        router.get("/verticles").handler(::showVerticles)

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port") ?: defaultPort)

        /* 部署dataVerticle */
        vertx.deployVerticle(DataVerticle::class.java, DeploymentOptions().setConfig(config().getJsonObject("jdbc"))) {
            if (it.succeeded()) {
                println("Succeeded in deploying verticle -- DataVerticle")
            } else {
                println("Failed in deploying verticle -- DataVerticle ${it.result()} ${it.cause().printStackTrace()}")
            }

        }
    }
    
    override fun stop() {
        super.stop()
    }

}