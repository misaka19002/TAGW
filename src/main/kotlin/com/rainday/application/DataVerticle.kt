package com.rainday.application

import com.rainday.`val`.*
import com.rainday.dto.ApplicationDto
import com.rainday.exception.TagwException
import com.rainday.gen.Tables.APPLICATION
import com.rainday.gen.tables.daos.ApplicationDao
import com.rainday.gen.tables.daos.ParampairDao
import com.rainday.gen.tables.daos.RelayDao
import com.rainday.gen.tables.pojos.Application
import com.rainday.model.Code
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameStyle
import org.jooq.conf.Settings
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
        //h2命名方式:upper
        val settings = Settings().withRenderNameStyle(RenderNameStyle.UPPER) // Defaults to QUOTED
        DSL.using(dataSource, SQLDialect.H2, settings)
    }
    private val eventBus by lazy { vertx.eventBus() }

    override fun init(vertx: Vertx?, context: Context?) {
        super.init(vertx, context)
        println("DataVerticle init")
    }

    override fun start() {
        /**
         * error code
         * err500->app verticle deploy exception
         *
         * err510->app verticle exist
         * */
        super.start()
        //appDeploy 成功记录 app 信息
        eventBus.consumer<JsonObject>(EB_APP_DEPLOY) {
            vertx.executeBlocking<Any>({ future ->
                val appDto = it.body().mapTo(ApplicationDto::class.java)
                val count = dslContext.fetchCount(
                    APPLICATION, APPLICATION.APP_KEY.eq(appDto.appKey)
                        .or(APPLICATION.APP_NAME.eq(appDto.appName))
                        .or(APPLICATION.APP_PORT.eq(appDto.appPort))
                )

                //如果查询到部署记录则直接返回部署失败。
                if (count > 0) {
                    future.fail(TagwException(Code.app510))
                } else {
                    dslContext.transaction { txContext ->
                        //todo 根据APPkey， 端口判断，任意一个已经存在，那么返回null。数据库新增APP信息
                        //app insert
                        ApplicationDao(txContext).insert(appDto)

                        //relay insert
                        /*val relayList = appDto.relays.forEach { relayDto ->
                            relayDto.appId = appDto.id
                            RelayDao(txContext).insert(relayDto)
                            relayDto.parampairs.forEach {
                                ParampairDao(txContext).insert(it)
                            }
                        }*/
                        //relay insert
                        val relayList = appDto.relays.map {
                            it.appId = appDto.id
                            return@map it
                        }
                        RelayDao(txContext).insert(relayList)

                        //parampair insert
                        relayList.forEach { relayDto ->
                            val parampairList = relayDto.parampairs.map {
                                it.relayId = relayDto.id
                                return@map it
                            }
                            ParampairDao(txContext).insert(parampairList)
                        }
                    }
                    future.complete(appDto.id)
                }
            }, { r ->
                if (r.succeeded()) {
                    it.reply(r.result())
                } else {
                    //如果失败打印日志
                    logger.error("EB_APP_DEPLOY 异常，", r.cause())
                    val cause = r.cause()
                    when (cause) {
                        is TagwException -> it.fail(cause.value, cause.message)
                        else -> it.fail(Code.app500.value, cause.message)
                    }
                }
            })
        }

        //update deployId
        eventBus.consumer<JsonObject>(EB_APP_UPDATEDEPLOYID) {
            vertx.executeBlocking<Any>({ future ->
                val appDto = it.body().mapTo(Application::class.java)
                val count = dslContext.update(APPLICATION).set(
                    APPLICATION.newRecord().apply { this.id = appDto.id; this.deployId = appDto.deployId }
                ).execute()
                future.complete(count)
            }, { r ->
                if (r.succeeded()) {
                    it.reply(r.result())
                } else {
                    it.fail(Code.app500.value, "")
                }
            })
            /*ApplicationDao().update(
                Application().apply { this.id = appDto.id; this.deployId = deployId }
            )*/
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