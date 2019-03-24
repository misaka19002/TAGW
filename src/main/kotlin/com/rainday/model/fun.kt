package com.rainday.model

import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import java.net.URI
import java.net.URL


fun Route.toPath(routeExtInfoMap: HashMap<Int, Relay>): URL? {
    val info = routeExtInfoMap[this.hashCode()]
    return info?.outUrl
}

fun Route.toMethod(routeExtInfoMap: HashMap<Int, Relay>): HttpMethod {
    val info = routeExtInfoMap[this.hashCode()]
    return info?.outMethod?:HttpMethod.GET
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