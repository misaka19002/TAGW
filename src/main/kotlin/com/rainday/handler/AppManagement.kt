package com.rainday.handler

import com.rainday.`val`.*
import com.rainday.application.AppVerticle
import com.rainday.ext.toJsonObject
import com.rainday.model.Application
import com.rainday.model.Status
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import javax.ws.rs.core.Response

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
            val deployOption = DeploymentOptions().setConfig(application.toJsonObject())
            vertx.deployVerticle(AppVerticle::class.java.name, deployOption) {
                if (it.succeeded()) {
                    //设置deployId
                    application.deployId = it.result()

                    val json = JsonObject.mapFrom(application)
                    vertx.eventBus().send(EB_APP_DEPLOY, json)
                    rc.response().setStatusCode(Response.Status.CREATED.statusCode)
                        .setStatusMessage(Response.Status.CREATED.name)
                        .putHeader("location", "/apps/${application.deployId}")
                        .end()
                } else {
                    rc.response().end("部署失败.原因：${it.cause().message}")
                }
            }
        } else {
            rc.response().setStatusCode(Response.Status.OK.statusCode)
                .setStatusMessage(Response.Status.OK.name)
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
        app
        it.result().body().deployId
    }

}

/**
 * 查询所有verticle
 */
fun showVerticles(rc: RoutingContext) {
    rc.response().end(rc.vertx().deploymentIDs().toString())
}