package com.rainday.application

import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.model.application.Application
import io.vertx.core.AbstractVerticle

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class ProjectInfoVerticle : AbstractVerticle() {

    val piMap = HashMap<String, Any>()
    val eventBus = vertx.eventBus()

    override fun start() {
        super.start()
        eventBus.localConsumer<Application>(EB_APP_DEPLOY){
            val application = it.body()
            println("consumer received ${application.deployId}")
            piMap.set(application.deployId, application)
        }
    }
}