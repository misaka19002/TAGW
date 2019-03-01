package com.rainday.model

/**
 * Created by wyd on 2019/2/28 17:29:42.
 */
/**
 *  @param prefix path前缀
 *  @param path 请求路径
 *  @param variables 参数列表
 *  */
data class Outside(
    val prefix: String,
    val path: String,
    val variables: List<Variable>
)

/**
 * @param type0 参数类型
 * @param type 参数类型
 * @param name0 收到的参数
 * @param name1 中继至后端server时，对应的参数名
 */
data class Variable(
    val type0: VarType,
    val name0: String,
    val name1: String,
    val type1: VarType
)

/**
 * 参数类型
 */
enum class VarType {
    PathVariable, RequestParam, RequestBody;
}