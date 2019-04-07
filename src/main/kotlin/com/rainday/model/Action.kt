package com.rainday.model

/**
 * Created by wyd on 2019/3/14 17:54:35.
 */
enum class Action {
    UPDATE_APPNAME,//修改APP名称
    UPDATE_APPDESCRIPTION,//修改APP描述
    UPDATE_RELAY,//修改APP中继(允许修改的参数为：outUrl,outMethod,transmission, paramPairs(参数对应关系))
    ADD_RELAY,//新增APP中继
    DELETE_RELAY,//删除APP中继
    ENABLE_RELAY,//启用relay(修改状态为route为enable)
    DISABLE_RELAY,//禁用relay(修改状态为route为disable)
    ACTIVE_APP,//激活APP
    INACTIVE_APP,//关闭APP
    
    UNKNOWN//未知的非法操作
}