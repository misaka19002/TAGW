package com.rainday.application

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.FIND_APP_BYID
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.`val`.QUERY_APP_BYNAME
import com.rainday.ext.toJsonObject
import com.rainday.model.Application
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class DataVerticle : AbstractVerticle() {

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

        //根据 appName 查询已发布vercicle 的信息
        eventBus.localConsumer<String>(FIND_APP_BYNAME){
            val appName = it.body()
            println("${FIND_APP_BYNAME} consumer received appName ${appName}")
            it.reply(piMap_appName[appName]?.toJsonObject())
        }

        eventBus.localConsumer<String>(FIND_APP_BYID){
            val deployId = it.body()
            println("${FIND_APP_BYID} consumer received deployId ${deployId}")
            it.reply(piMap_deployId[deployId]?.toJsonObject())
        }

        //查询app
        eventBus.localConsumer<String>(QUERY_APP_BYNAME){
            //todo 查询功能待完善
            it.reply(Json.encode(piMap_appName))
        }
    }
}