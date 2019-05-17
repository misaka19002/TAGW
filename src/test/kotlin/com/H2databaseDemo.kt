package com

import org.h2.jdbcx.JdbcConnectionPool
import java.util.*




/**
 *
 * @author wyd
 * @time  2019-04-05 21:41
 * @description
 */

fun main() {

    val dd = "aa".takeIf { it.length>1 }.run {

        println(this)
    }
    val dd1 = "aa".takeIf { it.length>2 }.run {
        println(this)
    }


    
    //数据库连接URL，当前连接的是目录下的gacl数据库
//    val JDBC_URL = "jdbc:h2:d:/Users/wyd/h2test"
    val JDBC_URL = "jdbc:h2:tcp://172.30.5.114:9092//srv/h2database/data/h2test"
    //连接数据库时使用的用户名
    val USERNAME = "h2admin"
    //连接数据库时使用的密码
    val PASSWORD = "123456"
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    val DRIVER_CLASS = "org.h2.Driver"
    
//    Class.forName(DRIVER_CLASS)
    //单个连接
//    val conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)
//    val stmt = conn.createStatement()
    //连接池
    val connectionPool = JdbcConnectionPool.create(JDBC_URL,USERNAME, PASSWORD)
    val stmt = connectionPool.getConnection().createStatement()
    //如果存在USER_INFO表就先删除USER_INFO表
    stmt.execute("DROP TABLE IF EXISTS USER_INFO")
    //创建USER_INFO表
    stmt.execute("CREATE TABLE USER_INFO(id VARCHAR(36) PRIMARY KEY,name VARCHAR(100),sex VARCHAR(4))")

    //创建tagw表
    stmt.execute("DROP TABLE IF EXISTS application")
    stmt.execute("DROP TABLE IF EXISTS relay")
    stmt.execute("DROP TABLE IF EXISTS parampair")
    stmt.execute("CREATE TABLE `application` (\n" +
        "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',\n" +
        "  `app_key` varchar(20) NOT NULL COMMENT 'appkey',\n" +
        "  `app_name` varchar(20) DEFAULT NULL COMMENT 'app名称',\n" +
        "  `app_port` int(11) NOT NULL COMMENT 'app端口',\n" +
        "  `description` varchar(255) DEFAULT NULL COMMENT 'app描述',\n" +
        "  `app_status` varchar(20) DEFAULT NULL COMMENT 'app状态',\n" +
        "  `deploy_id` varchar(36) DEFAULT NULL COMMENT 'deployId',\n" +
        "  PRIMARY KEY (`id`)\n" +
        ") ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;\n" +
        "\n" +
        "CREATE TABLE `relay` (\n" +
        "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',\n" +
        "  `app_id` int(11) DEFAULT NULL COMMENT 'application主键',\n" +
        "  `in_url` varchar(512) DEFAULT NULL COMMENT 'inbound uri',\n" +
        "  `in_method` varchar(512) DEFAULT NULL COMMENT 'inbound method',\n" +
        "  `out_url` varchar(512) DEFAULT NULL COMMENT 'outbound url',\n" +
        "  `out_method` varchar(512) DEFAULT NULL COMMENT 'outbound method',\n" +
        "  `transmission` smallint(1) DEFAULT NULL COMMENT '是否透传body',\n" +
        "  `relay_status` varchar(512) DEFAULT NULL COMMENT 'relay状态',\n" +
        "  PRIMARY KEY (`id`)\n" +
        ") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;\n" +
        "\n" +
        "CREATE TABLE `parampair` (\n" +
        "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',\n" +
        "  `relay_id` int(11) DEFAULT NULL COMMENT 'relayId',\n" +
        "  `in_name` varchar(20) DEFAULT NULL COMMENT '入参名称',\n" +
        "  `in_type` varchar(20) DEFAULT NULL COMMENT '入参类型',\n" +
        "  `out_name` varchar(20) DEFAULT NULL COMMENT '出参名称',\n" +
        "  `out_type` varchar(20) DEFAULT NULL COMMENT '出参类型',\n" +
        "  PRIMARY KEY (`id`)\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")

    //新增
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','大日如来','男')")
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','青龙','男')")
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','白虎','男')")
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','朱雀','女')")
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','玄武','男')")
    stmt.executeUpdate("INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','苍狼','男')")
    //删除
    stmt.executeUpdate("DELETE FROM USER_INFO WHERE name='大日如来'")
    //修改
    stmt.executeUpdate("UPDATE USER_INFO SET name='孤傲苍狼' WHERE name='苍狼'")
    
    //释放资源
    stmt.close()
    //关闭连接
    connectionPool.dispose()
}