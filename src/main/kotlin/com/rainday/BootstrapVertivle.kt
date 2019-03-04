package com.rainday

import com.rainday.application.AppVerticle
import com.rainday.handler.application.deployApp
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * Created by wyd on 2019/2/28 17:10:23.
 * BootstrapVerticle(MainVerticle)
 * deploy/undeploy  create/delete/update/query apps
 * 管理APP的verticle,在项目启动时就deploy，默认占用80端口。
 */
class BootstrapVertivle : AbstractVerticle() {

    val router = Router.router(vertx)

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
    }

    override fun start() {
        println(AppVerticle::class.java)
        println(AppVerticle::class.java.name)
        println(AppVerticle::javaClass.name)

        /* 部署application 管理 verticle */
        router.put("/app/add").handler(::deployApp)

        vertx.createHttpServer().listen(config().getInteger("http.port")).requestHandler(router)
    }
}