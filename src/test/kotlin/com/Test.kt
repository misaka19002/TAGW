package com

import com.rainday.model.UrlTemplate
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.net.URL
import java.util.regex.Pattern


/**
 *
 * @author wyd
 * @time  2019-03-22 21:15
 * @description
 */
val PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");

fun main() {
    val dsdf = json {
        this.obj(
            "aa" to "bb"
        )
    }

    val sdfdsf = JsonObject().apply {
        "sdf" to "dsf"
    }

    val url = "http://baidu.com/:a:b"
//    println(UrlTemplate(URL(url)).setPathParam("a","").setPathParam("b","bb").toString())
//    println(UrlTemplate(URL(url)).setPathParam("a","aa").setPathParam("b","bb").setQueryParam(" ","?ds/dd").toString())
    val d = "${t1(url)}   ${t2(url)}  "

    val dd = ":a:b?:aa:bb/:aaa/:bbb"
    println(
        UrlTemplate(dd)
            .setPathParam("a", "a").setPathParam("b", "b")
            .setPathParam("aa", "aa").setPathParam("bb", "bb")
            .setPathParam("aaa", "aaa").setPathParam("bbb", "bbb")
            .toString()
    )


}

fun t1(url: String): String {
    val start = System.currentTimeMillis()
    val times = 100_0000
    for (a in 1..times) {
        UrlTemplate(url).setPathParam("a", "aa$a").setPathParam("b", "bb$a").toString()
    }
    val end = System.currentTimeMillis()
    return (end - start).toBigDecimal().divide(times.toBigDecimal()).toString()

}

fun t2(url: String): String {
    val start = System.currentTimeMillis()
    val times = 100_0000
    for (a in 1..times) {
        URL(url).toString()
    }
    val end = System.currentTimeMillis()
    return (end - start).toBigDecimal().divide(times.toBigDecimal()).toString()
}
