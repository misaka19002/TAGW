package com.rainday.model

import java.net.URLEncoder
import java.util.regex.Pattern

data class UrlTemplate(val rawUrl: String) {
    private val PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");
    val pathParamMap = mutableMapOf<String, String>()
    val queryParamMap = mutableMapOf<String, String>()
    val headerParamMap = mutableMapOf<String, String>()

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

    fun setHeaderParam(name: String, value: String): UrlTemplate {
        queryParamMap.put(
            URLEncoder.encode(name, Charsets.UTF_8.name()),
            URLEncoder.encode(value, Charsets.UTF_8.name())
        )
        return this
    }

    fun setParam(paramType: ParamType, name: String, value: String): UrlTemplate {
        when (paramType) {
            ParamType.path -> setPathParam(name, value)
            ParamType.query -> setQueryParam(name, value)
            ParamType.header -> setHeaderParam(name, value)
        }
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