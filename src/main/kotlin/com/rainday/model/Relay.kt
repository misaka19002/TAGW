package com.rainday.model

import com.rainday.annotation.noArgs

/**
 * Created by wyd on 2019/2/28 17:29:42.
 */
@noArgs
data class Relay(var outUrl: String, var transmission: Boolean, var status: Status, var paramPairs: List<ParamPair>)

@noArgs
data class ParamPair(var inName: String, var inType: ParamType, var outName: String, var outType: ParamType)
