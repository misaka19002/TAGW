package com.rainday.`val`

import com.rainday.model.Application
import com.rainday.model.Relay
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by wyd on 2019/3/1 13:35:50.
 */

//路由扩展信息，用来存储route.class的inbound信息及相关信息
val routeExtInfoMap = HashMap<Int, Relay>()
//存储当前所有APP
val appExtInfoMap = HashMap<String, Application>()
val EB_APP_DEPLOY = "app-management-deploy"
val EB_APP_UNDEPLOY = "app-management-undeploy"

val QUERY_APP_BYNAME = UUID.randomUUID().toString()
val FIND_APP_BYNAME = UUID.randomUUID().toString()
val FIND_APP_BYID = UUID.randomUUID().toString()

val DEFAULT_USERAGENT = "TAGW/1.0.0"