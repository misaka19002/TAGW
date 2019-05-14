package com.rainday.application

import com.rainday.`val`.EB_APP_DELETE
import com.rainday.`val`.EB_APP_DEPLOY
import com.rainday.`val`.EB_APP_UNDEPLOY
import com.rainday.`val`.FIND_APP_BYNAME
import com.rainday.dto.DeployDto
import com.rainday.gen.Tables
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.h2.tools.Server
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
            this.maximumPoolSize = 10
            this.minimumIdle = 2
            connectionTimeout = 60000
            idleTimeout = 60000
        }
    }
    private val dslContext by lazy {
        DSL.using(dataSource,SQLDialect.H2)
    }
    private val eventBus by lazy { vertx.eventBus() }

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        println("DataVerticle init")
    }
    
    override fun start() {
        super.start()
        //appDeploy 成功记录 app 信息
        eventBus.consumer<JsonObject>(EB_APP_DEPLOY) {
            //todo 根据APPkey， 端口判断，任意一个已经存在，那么返回null。数据库新增APP信息
            val deployDto = it.body().mapTo(DeployDto::class.java)
            println(Json.encode(deployDto))
            try {
                Server.createPgServer()
                dslContext.transaction { txContext->
                    //DSL.using(txBeginner)
                    var appRecord = Tables.APPLICATION.newRecord().apply {
                        this.from(deployDto)
                    }
                    appRecord = DSL.using(txContext).insertInto(Tables.APPLICATION).values(appRecord).returning(Tables.APPLICATION.ID).fetchOne()
                    val relayList = deployDto.relays.forEach {
                        //save relay
                        val relayRecord = Tables.RELAY.newRecord().apply {
                            this.from(it)
                            this.appId = appRecord.id
                        }
                        DSL.using(txContext).insertInto(Tables.RELAY).values(relayRecord).returning(Tables.RELAY.ID).execute()
                        //save parampair
                        it.parampairs.forEach {
                            val pair = Tables.PARAMPAIR.newRecord().apply {
                                this.from(it)
                                this.relayId = relayRecord.id
                            }
                            DSL.using(txContext).insertInto(Tables.PARAMPAIR).values(pair).returning(Tables.PARAMPAIR.ID).execute()
                        }
                    }
                }
                it.reply("success")
            } catch (e: Exception) {
                logger.error("保存APPkey异常", e)
                it.reply("error")
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