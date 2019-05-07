package com

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import java.util.*

/**
 * Created by wyd on 2019/4/24 13:31:32.
 */
fun main(args: Array<String>) {
    val vertx = Vertx.vertx()


    vertx.sharedData().getAsyncMap<String, Any>("testmap") {
        it.result().put("sdf", Date()) {}
    }


    val f1 = Future.future<AsyncMap<String, Any>>()
    vertx.sharedData().getAsyncMap("testmap", f1)
    vertx.runOnContext {
        f1.compose {
            val d = Future.future<Void>()
            println(Thread.currentThread())
            it.put("aa", Date(), d)
            return@compose d
        }.compose {
            println(Thread.currentThread())
            return@compose Future.succeededFuture(Date())
        }
    }

    println("11")

    val asyncMapFuture = Future.future<AsyncMap<String,Any>>()
    vertx.sharedData().getAsyncMap("testmap",asyncMapFuture)

    println("sdfsdf")
    asyncMapFuture.result().size{
        println(it.result())
    }
    asyncMapFuture.result().get("aa"){
        println(it.result())
    }

    f1.result().size {
        println(it.result())
    }

}