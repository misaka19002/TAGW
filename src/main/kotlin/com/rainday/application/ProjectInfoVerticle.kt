package com.rainday.application

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_APP
import com.rainday.`val`.QUERY_APP
import com.rainday.model.Application
import com.rainday.model.Relay
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class ProjectInfoVerticle : AbstractVerticle() {

    private val piMap_deployId = HashMap<String, Application>()
    private val piMap_appName = HashMap< String, Application>()
    private val eventBus by lazy { vertx.eventBus() }

    override fun start() {
        super.start()
        //verticledeploy 成功记录verticle信息
        eventBus.localConsumer<JsonObject>(EB_APP_DEPLOY){
            val application = it.body().mapTo(Application::class.java)

            println("${EB_APP_DEPLOY} consumer received ${application.deployId}")

            piMap_deployId[application.deployId] = application
            piMap_appName[application.appName] = application
        }

        //根据 deployId 查询已发布vercicle 的信息
        eventBus.localConsumer<String>(FIND_APP){
            val deployId = it.body()
            println("${FIND_APP} consumer received deployId ${deployId}")
            it.reply(piMap_deployId[deployId])
        }

        //查询app
        eventBus.localConsumer<String>(QUERY_APP){
            //todo 查询功能待完善
            it.reply(Json.encode(piMap_appName))
        }
    }
}