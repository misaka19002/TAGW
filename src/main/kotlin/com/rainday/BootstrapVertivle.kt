package com.rainday

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router

/**
 * Created by wyd on 2019/2/28 17:10:23.
 * BootstrapVerticle(MainVerticle) instanceof CoroutineVerticle
 */
class BootstrapVertivle : AbstractVerticle() {

    val router = Router.router(this.vertx)


    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }
}