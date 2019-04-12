package com.rainday.model

import com.rainday.`val`.VERTICLE_INFO
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.impl.ContextInternal
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import java.net.URL


fun Route.toPath(routeExtInfoMap: HashMap<Int, Relay>): URL? {
    val info = routeExtInfoMap[this.hashCode()]
    return info?.outUrl
}

fun Route.toMethod(routeExtInfoMap: HashMap<Int, Relay>): HttpMethod {
    val info = routeExtInfoMap[this.hashCode()]
    return info?.outMethod ?: HttpMethod.GET
}

fun Route.getBindInfo(routeExtInfoMap: HashMap<Int, Relay>): Relay? {
    return routeExtInfoMap[this.hashCode()]
}

fun RoutingContext.getParameter(paramType: ParamType, paramName: String): String {
    return when (paramType) {
        ParamType.path -> this.pathParam(paramName)
        ParamType.query -> this.queryParams().get(paramName)
        ParamType.header -> this.request().getHeader(paramName)
    }
}

/**
 * @author wyd
 * @date  2019-03-30 20:28
 * @param vertx : vertx
 * @param context : verticle deploy context
 * @return : null
 */
fun addVerticleDeployInfo(vertx: Vertx, context: Context) {
    val json = JsonObject(
        "deployId" to context.deploymentID(),
        "instanceCount" to context.instanceCount,
        "eventLoopContext" to context.isEventLoopContext,
        "workerContext" to context.isWorkerContext,
        "multiThreadedWorkerContext" to context.isMultiThreadedWorkerContext,
        "config" to context.config(),
        "identifier" to (context as ContextInternal).deployment.verticleIdentifier()
    )
    vertx.globalPut(VERTICLE_INFO,context.deploymentID(), json)
}

/**
 * @author wyd
 * @date  2019-03-30 20:28
 * @param vertx : vertx
 * @param deployId : remove verticle by deloyId
 * @return : null
 */
fun deleteVerticleDeployInfo(vertx: Vertx, deployId: String) {
    vertx.globalRemove(VERTICLE_INFO, deployId)
}

/**
 * @author wyd
 * @date  2019-04-09 21:43
 * @param mapName : 全局map的Name
 * @param key : mapName中需要被移除的key
 * @return : null
 */
fun Vertx.globalRemove(mapName: String, key: String) {
    if (this.isClustered) {
        this.sharedData().getClusterWideMap<String, JsonObject>(mapName) {
            it.result().remove(key) {}
        }
    } else {
        val localMap = this.sharedData().getLocalMap<String, JsonObject>(mapName)
        localMap.remove(key)
    }
}

/**
 * @author wyd
 * @date  2019-04-09 21:44
 * @param mapName : 全局map的name
 * @param key : mapName中需要增加的key
 * @param value : key对应的value
 * @return : null
 */
fun Vertx.globalPut(mapName: String, key: String, value: Any) {
    if (this.isClustered) {
        this.sharedData().getClusterWideMap<String, JsonObject>(mapName) {
            it.result().remove(key) {}
        }
    } else {
        val localMap = this.sharedData().getLocalMap<String, JsonObject>(mapName)
        localMap.remove(key)
    }
}

fun Vertx.globalGet(mapName: String, key: String):Any? {
    return if (this.isClustered) {
        val resFuture:Future<Any?> = Future.future()
        this.sharedData().getClusterWideMap<String, JsonObject>(mapName) {
            it.result().get(key) {
                resFuture.complete(it.result())
            }
        }
        resFuture.result()
    } else {
        val localMap = this.sharedData().getLocalMap<String, JsonObject>(mapName)
        localMap.get(key)
    }
}