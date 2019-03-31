package com.rainday.model

import com.rainday.`val`.VERTICLE_INFO
import io.vertx.core.Context
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

fun Route.bindInfo(relay: Relay, routeExtInfoMap: HashMap<Int, Relay>): Route {
    routeExtInfoMap[this.hashCode()] = relay
    return this
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
    if (vertx.isClustered) {
        val clusterMap = vertx.sharedData().getClusterWideMap<String, JsonObject>(VERTICLE_INFO) {
            //get verticle_info map, save info
            it.result().put(context.deploymentID(), json) {}
        }
    } else {
        val localMap = vertx.sharedData().getLocalMap<String, JsonObject>(VERTICLE_INFO)
        localMap.put(context.deploymentID(), json)
    }
}

/**
 * @author wyd
 * @date  2019-03-30 20:28
 * @param vertx : vertx
 * @param deployId : remove verticle by deloyId
 * @return : null
 */
fun deleteVerticleDeployInfo(vertx: Vertx, deployId: String) {
    if (vertx.isClustered) {
        val clusterMap = vertx.sharedData().getClusterWideMap<String, JsonObject>(VERTICLE_INFO) {
            //get verticle_info map, delete verticle info
            it.result().remove(deployId) {}
        }
    } else {
        val localMap = vertx.sharedData().getLocalMap<String, JsonObject>(VERTICLE_INFO)
        localMap.remove(deployId)
    }
}