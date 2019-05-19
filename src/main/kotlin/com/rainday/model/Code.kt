package com.rainday.model

/**
 * Created by wyd on 2019/5/16 11:04:17.
 */
enum class Code(val value: Int, var msg: String? = "") {
    /**
     * error code
     * 500->system exception,app verticle deploy exception
     *
     * 510->app verticle exist
     * */
    app200(200, "app verticle deploy successfully"),
    app500(500, "app verticle deploy exception"),
    app510(510, "app verticle exist"),

    relay404(404, "relay url not exist"),

    app600(100);

    val svalue: String = value.toString()

}