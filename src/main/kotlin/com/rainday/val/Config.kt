package com.rainday.`val`

import com.rainday.model.Relay
import java.util.*

/**
 * Created by wyd on 2019/3/1 13:35:50.
 */

val routeExtInfoMap = HashMap<Int, Relay>()

val EB_APP_DEPLOY = "app-management-deploy"
val EB_APP_UNDEPLOY = "app-management-undeploy"

val QUERY_APP = UUID.randomUUID().toString()
val FIND_APP = UUID.randomUUID().toString()