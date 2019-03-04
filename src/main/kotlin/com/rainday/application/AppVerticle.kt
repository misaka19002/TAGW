package com.rainday.application

import com.sun.org.apache.bcel.internal.util.ClassPath
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * Created by wyd on 2019/3/1 10:16:53.
 * application management verticle
 * deploy/undeploy app
 */
class AppVerticle: AbstractVerticle() {
    val classPath = ClassPath.getClassPath()
    val sysClassPath = ClassPath.SYSTEM_CLASS_PATH

    val router = Router.router(vertx)
    val appPrefix = "/app"


    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        println(classPath)
        println(sysClassPath)
    }

    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }
}