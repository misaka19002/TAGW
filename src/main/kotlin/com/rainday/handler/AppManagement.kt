package com.rainday.handler

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.`val`.QUERY_APP_BYNAME
import com.rainday.`val`.VERTICLE_INFO
import com.rainday.application.AppVerticle
import com.rainday.ext.toJsonObject
import com.rainday.ext.toJsonString
import com.rainday.model.Application
import com.rainday.model.Status
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Created by wyd on 2019/3/1 11:28:33.
 */

/**
 * 部署app
 */
fun deployApp(rc: RoutingContext) {
    val vertx = rc.vertx()
    val application = rc.bodyAsJson.mapTo(Application::class.java)
    //deploy之前先查询，如果已经存在则直接返回
    vertx.eventBus().send<JsonObject>(FIND_APP_BYNAME, application.appName) {
        if (it.succeeded() && it.result().body() == null) {
            //不存在继续deploy
            val deployOption = DeploymentOptions().setConfig(application.toJsonObject()).setInstances(3)
            vertx.deployVerticle(AppVerticle::class.java.name, deployOption) {
                if (it.succeeded()) {
                    //设置deployId
                    application.deployId = it.result()

                    val json = JsonObject.mapFrom(application)
                    vertx.eventBus().send(EB_APP_DEPLOY, json)
                    rc.response().setStatusCode(HttpResponseStatus.CREATED.code())
                        .setStatusMessage(HttpResponseStatus.CREATED.reasonPhrase())
                        .putHeader("location", "/apps/${application.deployId}")
                        .end()
                } else {
                    rc.response().end("部署失败.原因：${it.cause().message}")
                }
            }
        } else {
            rc.response().setStatusCode(HttpResponseStatus.OK.code())
                .setStatusMessage(HttpResponseStatus.OK.reasonPhrase())
                .end(it.result().body().toString())
        }
    }
}

/**
 * 更新APP状态，根据action执行不同逻辑
 */
fun updateApp(rc: RoutingContext) {
    val vertx = rc.vertx()
    val request = rc.request()

    val status = Status.valueOf(request.getHeader("status"))
    val deployId = rc.pathParam("deployId")
    //todo 这里校验update的app必须是存在的。
    when (status) {
        //deploy app
        Status.active -> {
            //todo 获取deployOption projectInfoVerticle
            //step1 : 根据id获取application信息
            val application = rc.bodyAsJson.mapTo(Application::class.java)
            val deployOption = DeploymentOptions().setConfig(JsonObject(Json.encode(application)))
            vertx.deployVerticle(AppVerticle::class.java.name, deployOption) {
                if (it.succeeded()) {
                    rc.response().end("${status} 成功")
                } else {
                    rc.response().end("${status} 失败")
                }
            }
        }
        //undeploy app
        Status.inactive -> {
            vertx.undeploy(deployId) {
                if (it.succeeded()) {
                    rc.response().end("${status} 成功")
                } else {
                    rc.response().end("${status} 失败")
                }
            }
        }
    }
}

/**
 * 删除APP
 */
fun deleteApp(rc: RoutingContext) {
    val vertx = rc.vertx()
    rc.queryParams()
    rc.request().params()
    //todo 这里校验被删除的APP必须是inactive的
    val deployId = rc.pathParam("deployId")
    //todo 这里删除projectInfoVerticle里的APP数据
//    vertx.eventBus().send("")
}

/**
 * 根据条件查询APP
 */
fun queryApp(rc: RoutingContext) {
    rc.vertx().eventBus().send<String>(QUERY_APP_BYNAME, "") {
        rc.response().end(it.result().body())
    }
}

/**
 * 查找某个确定的APP
 */
fun findApp(rc: RoutingContext) {
    val deployId = rc.pathParam("deployId")
    rc.vertx().eventBus().send<String>(FIND_APP_BYNAME, deployId) {
        rc.response().end(it.result().body())
    }
}

/**
 * app唯一性校验
 */
fun appUniqueCheck(rc: RoutingContext) {
    val vertx = rc.vertx()
    val appName: String by rc.pathParams()
    vertx.eventBus().send<Application>(EB_APP_DEPLOY, appName) {
        println("echo received deploy info ${it.result().body()}")
        val app = it.result().body()
        it.result().body().deployId
    }

}

/**
 * 查询所有verticle
 */
fun showVerticles(rc: RoutingContext) {
    val vertx = rc.vertx()
    if (vertx.isClustered) {
        vertx.sharedData().getClusterWideMap<String, JsonObject>(VERTICLE_INFO) {
            rc.response().end(it.result().toJsonString())
        }
    } else {
        val localMap = vertx.sharedData().getLocalMap<String, JsonObject>(VERTICLE_INFO)
        rc.response().end(localMap.toJsonString())
    }
}