package com.rainday.ext

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/3/1 13:08:32.
 */
fun Any.toJsonString(): String {
    return Json.encode(this)
}

fun Any.toJsonObject(): JsonObject {
    return JsonObject.mapFrom(this)
}

fun List<Any>.toJsonArray(): JsonArray {
    return JsonArray(this)
}

fun String.toJsonArray(): JsonArray {
    return JsonArray(this)
}

fun Buffer.toJsonArray(): JsonArray {
    return JsonArray(this)
}