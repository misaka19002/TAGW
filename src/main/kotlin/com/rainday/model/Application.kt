package com.rainday.model

import com.rainday.annotation.noArgs

/**
 * Created by wyd on 2019/3/1 10:17:58.
 */
/**
 * @param appName apiGateway名称
 * @param port app占用的端口
 * @param description app的介绍
 * @param deployId 部署ID
 * @param status APP状态
 * @param relays 当前app中管理的接口列表(可空，此时相当于代理)
 */
@noArgs
data class Application(
    val appName: String,
    val port: Int,
    val description: String,
    var deployId: String,
    var status: Status,
    var relays: List<Relay>
)