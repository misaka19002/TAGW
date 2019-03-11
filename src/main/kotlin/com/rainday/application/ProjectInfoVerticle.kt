package com.rainday.application

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_ALL_APP
import com.rainday.`val`.FIND_ONE_APP
import com.rainday.model.application.Application
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class ProjectInfoVerticle : AbstractVerticle() {

    val piMap_deployId = HashMap<String, Application>()
    val piMap_appName = HashMap< String, Application>()
    val eventBus by lazy { vertx.eventBus() }

    override fun start() {
        super.start()
        //verticledeploy 成功记录verticle信息
        eventBus.localConsumer<JsonObject>(EB_APP_DEPLOY){
            val application = it.body().mapTo(Application::class.java)

            println("consumer received ${application.deployId}")

            piMap_deployId[application.deployId] = application
            piMap_appName[application.appName] = application
        }

        //根据appName查询已发布vercicle 的信息
        eventBus.localConsumer<String>(FIND_ONE_APP){
            val appName = it.body()
            println("consumer received ${appName}")
            it.reply(piMap_appName[appName])
        }

        //查询所有app
        eventBus.localConsumer<String>(FIND_ALL_APP){
            it.reply(Json.encode(piMap_appName))
        }
    }
}