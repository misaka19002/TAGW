package com.rainday.application

import com.rainday.`val`.*
import com.rainday.model.Application
import com.rainday.model.globalGet
import com.rainday.model.globalPut
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.jooq.SQL
import org.jooq.SQLDialect
import org.jooq.impl.DSL

/**
 * Created by wyd on 2019/3/1 13:31:53.
 */
class DataVerticle : AbstractVerticle() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val dataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = config().getString("url")
            username = config().getString("username")
            password = config().getString("password")
            connectionTimeout = 60000
            idleTimeout = 60000
        }
    }
    private val dslContext by lazy {
        DSL.using(dataSource,SQLDialect.H2)
    }
    private val eventBus by lazy { vertx.eventBus() }
    
    override fun start() {
        super.start()
        //appDeploy 成功记录 app 信息
        eventBus.consumer<JsonObject>(EB_APP_DEPLOY) {
            val application = it.body().mapTo(Application::class.java)
            logger.info("${EB_APP_DEPLOY} consumer received ${application.deployId}")
            vertx.globalPut(APPLICATION_INFO, application.deployId, it.body())
            //todo 数据库新增APP信息
        }
        
        //appUndeploy
        eventBus.consumer<JsonObject>(EB_APP_UNDEPLOY) {
            //todo 数据库，将APP改为inactive
        }
        
        eventBus.consumer<JsonObject>(EB_APP_DELETE) {
        
        }
        
        //根据 appName 查询已发布 app 的信息
        eventBus.consumer<String>(FIND_APP_BYNAME) {
            
            //todo 根据APPname查询数据库，如果APP已经存在则直接返回。
            val appName = it.body()
            println("${FIND_APP_BYNAME} consumer received appName ${appName}")
            val result = vertx.globalGet(APPLICATION_INFO, appName)
            it.reply(result)
        }
    }
}