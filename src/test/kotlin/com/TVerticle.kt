package com

import io.vertx.core.AbstractVerticle
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by wyd on 2019/4/26 14:48:43.
 */
class TVerticle:AbstractVerticle() {

    val counter = AtomicLong(1)

    override fun start() {
        super.start()
        println(counter)
        vertx.createHttpServer().requestHandler {
            it.response().end("${Thread.currentThread()} ${counter.getAndIncrement()} $this")
        }.listen(8080)
    }
}