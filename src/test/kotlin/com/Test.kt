package com

import java.net.URI
import java.net.URL
import java.util.regex.Pattern
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.HashMap
import java.util.ArrayList
import java.net.URISyntaxException
import java.util.stream.Collectors


/**
 *
 * @author wyd
 * @time  2019-03-22 21:15
 * @description
 */
val PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");

fun main() {

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

data class UrlTemplate(val rawUrl: String) {
    private val pathParamMap = mutableMapOf<String, String>()
    private val queryParamMap = mutableMapOf<String, String>()

    private val parsablePath = mutableListOf<String>()
    private val pathIndices = mutableMapOf<String, Int>()

    init {
        parseRawUrl(rawUrl)
    }

    private fun parseRawUrl(rawUrl: String) {
        val m = PLACEHOLDER.matcher(rawUrl)
        var index = 0
        while (m.find()) {
            val name = m.group(1)
            if (pathIndices.containsKey(name)) {
                throw IllegalArgumentException("$name has been used, please use another name")
            }
            parsablePath.add(rawUrl.substring(index, m.start()))
            parsablePath.add("")

            pathIndices.put(name, parsablePath.size - 1)
            index = m.end()
        }
        if (index < rawUrl.length - 1) {
            parsablePath.add(rawUrl.substring(index))
        }
    }

    fun setPathParam(name: String, value: String): UrlTemplate {
        pathParamMap.put(
            URLEncoder.encode(name, Charsets.UTF_8.name()),
            URLEncoder.encode(value, Charsets.UTF_8.name())
        )
        pathIndices[name]?.let {
            parsablePath[it] = value
        }
        return this
    }

    fun setQueryParam(name: String, value: String): UrlTemplate {
        queryParamMap.put(
            URLEncoder.encode(name, Charsets.UTF_8.name()),
            URLEncoder.encode(value, Charsets.UTF_8.name())
        )
        return this
    }

    override fun toString(): String {
        val parsedPath = parsablePath.joinToString("")
        return if (queryParamMap.isEmpty()) {
            parsedPath
        } else {
            //encoded - parsedQuery
            val parsedQuery = queryParamMap.map { "${it.key}=${it.value}" }.joinToString("&")
            "$parsedPath?$parsedQuery"
        }
    }
}