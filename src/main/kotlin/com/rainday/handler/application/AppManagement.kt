package com.rainday.handler.application

import com.rainday.EB_APP_DEPLOY
import com.rainday.application.AppVerticle
import com.rainday.model.application.Application
import io.vertx.core.DeploymentOptions
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

    val deployOption = DeploymentOptions().setConfig(JsonObject.mapFrom(application))
    vertx.deployVerticle(AppVerticle::class.java, deployOption) {
        if (it.succeeded()) {
            //设置deployId
            application.deployId = it.result()
            vertx.eventBus().send<Application>(EB_APP_DEPLOY, application) {
                //todo application deploy successfully and all the actions are done.
                println("echo received deploy info ${it.result().body().toString()}")
            }
        } else {
            //todo log this error
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
fun allApp(rc: RoutingContext) {

}

/**
 * 根据APP名称 查询符合条件的APP
 */
fun queryApp(rc: RoutingContext) {

}