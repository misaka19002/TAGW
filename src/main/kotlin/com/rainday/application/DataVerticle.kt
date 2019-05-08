package com.rainday.application

import com.rainday.`val`.EB_APP_DELETE
import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.EB_APP_UNDEPLOY
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.dto.DeployDto
import com.rainday.gen.Tables
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
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
            //todo 根据APPkey， 端口判断，任意一个已经存在，那么返回null。数据库新增APP信息
            val deployDto = it.body().mapTo(DeployDto::class.java)
            dslContext.transaction { txBeginner->
                //DSL.using(txBeginner)
                val application = Tables.APPLICATION.newRecord().apply {
                    this.from(deployDto)
                }
                val relayList = deployDto.relays.map {
                    Tables.RELAY.newRecord().apply {
                        this.from(it)
                    }
                }
                val parampairList = deployDto.relays.map {
                    it.parampairs.ma
                }

            }
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
        }
    }
}