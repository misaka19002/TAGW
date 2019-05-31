package com.rainday.model

import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.get


fun RoutingContext.getParameter(paramType: ParamType, paramName: String): String? {
    return when (paramType) {
        ParamType.path -> this.pathParam(paramName)
        ParamType.query -> this.queryParams()?.get(paramName)
        ParamType.header -> this.request().getHeader(paramName)
        ParamType.body -> this.bodyAsJson?.get(paramName)
    }
}
