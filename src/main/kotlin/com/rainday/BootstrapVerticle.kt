package com.rainday

import com.rainday.application.ProjectInfoVerticle
import com.rainday.handler.Global404Handler
import com.rainday.handler.Global413Handler
import com.rainday.handler.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * Created by wyd on 2019/2/28 17:10:23.
 * BootstrapVerticle(MainVerticle)
 * deploy/undeploy  create/delete/update/query apps
 * 管理APP的verticle,在项目启动时就deploy，默认占用80端口。
 */
class BootstrapVerticle : AbstractVerticle() {

    private val router by lazy { Router.router(vertx) }

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
    }

    override fun start() {
        /* 部署application 管理 verticle */
        router.errorHandler(Response.Status.NOT_FOUND.statusCode, ::Global404Handler)
        router.errorHandler(Response.Status.REQUEST_ENTITY_TOO_LARGE.statusCode, ::Global413Handler)
        router.route("/*").handler(BodyHandler.create())
        router.put("/app/add").consumes(MediaType.APPLICATION_JSON).handler(::deployApp)
        router.get("/apps").handler(::allApps)
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port"))
        /* 部署projectInfoVerticle */
        vertx.deployVerticle(ProjectInfoVerticle()){
            if (it.succeeded()) {
                println("tagw starting 。。")
            } else {
                it.cause().printStackTrace()
            }
        }
    }

}