package com.rainday.application

import com.rainday.`val`.APPLICATION_INFO
import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.EB_APP_UNDEPLOY
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.model.Application
import com.rainday.model.globalGet
import com.rainday.model.globalPut
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class DataVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    
    private val eventBus by lazy { vertx.eventBus() }
    
    override fun start() {
        super.start()
        //appDeploy 成功记录 app 信息
        eventBus.consumer<JsonObject>(EB_APP_DEPLOY) {
            val application = it.body().mapTo(Application::class.java)
            logger.info("${EB_APP_DEPLOY} consumer received ${application.deployId}")
            vertx.globalPut(APPLICATION_INFO, application.appName, it.body())
            //todo 数据库新增APP信息
        }
        
        //appUndeploy 删除APPinfo app 信息
        eventBus.consumer<JsonObject>(EB_APP_UNDEPLOY) {
            //todo 数据库删除APP信息
        }
        
        //根据 appName 查询已发布 app 的信息
        eventBus.consumer<String>(FIND_APP_BYNAME) {
            val appName = it.body()
            println("${FIND_APP_BYNAME} consumer received appName ${appName}")
            val result = vertx.globalGet(APPLICATION_INFO, appName)
            it.reply(result)
        }
    }
}