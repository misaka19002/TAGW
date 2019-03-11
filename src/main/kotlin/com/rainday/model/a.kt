package com.rainday.model

import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route


fun Route.toPath(routeExtInfoMap: HashMap<Int, JsonObject>): String {
    val info = routeExtInfoMap[this.hashCode()]
    return info?.getString("toPath") ?: ""
}

fun Route.toMethod(routeExtInfoMap: HashMap<Int, JsonObject>): HttpMethod {
    val info = routeExtInfoMap[this.hashCode()]
    val method = info?.getString("toMethod") ?: ""
    return HttpMethod.valueOf(method)
}

fun Route.bindInfo(jsonObject: JsonObject, routeExtInfoMap: HashMap<Int, JsonObject>): Route {
    routeExtInfoMap[this.hashCode()] = jsonObject
    return this
}

fun Route.getBindInfo(routeExtInfoMap: HashMap<Int, JsonObject>): JsonObject {
    return routeExtInfoMap[this.hashCode()] ?: JsonObject()
}