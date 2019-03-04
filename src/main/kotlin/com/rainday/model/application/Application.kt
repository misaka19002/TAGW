package com.rainday.model.application

/**
 * Created by wyd on 2019/3/1 10:17:58.
 */
/**
 * @param appName apiGateway名称
 * @param port app占用的端口
 * @param description app的介绍
 */
data class Application(
    val appName: String,
    val port: Int,
    val description: String,
    var deployId: String = ""
)