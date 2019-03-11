package com.rainday.handler

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

/* 未找到资源 */
fun Global404Handler(rc: RoutingContext) {
    // Send back default 404
    rc.response().setStatusMessage("Not Found ~").setStatusCode(404)
    if (rc.request().method() == HttpMethod.HEAD) {
        // HEAD responses don't have a body
        rc.response().end()
    } else {
        rc.response()
            .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
            .end("<html><body><h1>Resource not found!~`~</h1></body></html>")
    }
}

/* body超过限制 */
fun Global413Handler(rc: RoutingContext) {
    // Send back default 413
    rc.response().setStatusMessage("Body too large").setStatusCode(413)
    if (rc.request().method() == HttpMethod.HEAD) {
        // HEAD responses don't have a body
        rc.response().end()
    } else {
        rc.response()
            .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
            .end("<html><body><h1>Body too large!~`~</h1></body></html>")
    }
}
