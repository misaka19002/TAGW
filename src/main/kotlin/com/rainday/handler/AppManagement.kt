package com.rainday.handler

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_ALL_APP
import com.rainday.application.AppVerticle
import com.rainday.model.application.Application
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
    val application = rc.bodyAsJson.mapTo(Application::class.java)
    val vertx = rc.vertx()

    val deployOption = DeploymentOptions().setConfig(JsonObject(Json.encode(application)))
    vertx.deployVerticle(AppVerticle::class.java.name, deployOption) {
        if (it.succeeded()) {
            //设置deployId
            application.deployId = it.result()

            val json = JsonObject.mapFrom(application)
            vertx.eventBus().send(EB_APP_DEPLOY, json)
            rc.response().end(json.toString())
        } else {
            rc.response().end("部署失败")
        }
    }
}

/**
 * 卸载并关闭APP,undeploy verticle
 */
fun unDeployApp(rc: RoutingContext) {

}

/**
 * 查看所有的APP
 */
fun allApps(rc: RoutingContext) {
    rc.vertx().eventBus().send<String>(FIND_ALL_APP, "") {
        rc.response().end(it.result().body())
    }
}

/**
 * 根据APP名称 查询符合条件的APP
 */
fun queryApp(rc: RoutingContext) {


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