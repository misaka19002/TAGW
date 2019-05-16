package com.rainday.exception

import com.rainday.model.Code

/**
 * Created by wyd on 2019/5/16 14:04:25.
 */
class TagwException(val code: Code) : RuntimeException(code.msg) {
    val value by lazy { code.value }
    val svalue by lazy { code.svalue }

}

/*
class TagwException : RuntimeException {
    var code = Code.app500
    val value by lazy { code.value }
    val svalue by lazy { code.svalue }

    constructor(code: Code) : super(code.msg) {
        this.code = code
    }
}
*/
