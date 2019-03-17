package com.rainday.model

import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
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


fun main() {
    val d = "sss.com/usr/ddf.do?aa=dd&bb=a"
    val uri = URI(d)
//    val url = URL(d)
    println(uri.toString())
//    println(url.toString())
//    println(uri.toURL().toString())
//    println(url.toURI().toString())
}