package com.rainday.model

import com.rainday.annotation.noArgs
import io.vertx.core.http.HttpMethod
import java.net.URI
import java.net.URL

/**
 * Created by wyd on 2019/2/28 17:29:42.
 */
@noArgs
data class Relay(
    var inUrl: URI,
    var inMethod: HttpMethod,
    var outUrl: URL,
    var outMethod: HttpMethod,
    var transmission: Boolean,
    var status: Status,
    var paramPairs: List<ParamPair>
)

@noArgs
data class ParamPair(var inName: String, var inType: ParamType, var outName: String, var outType: ParamType)
