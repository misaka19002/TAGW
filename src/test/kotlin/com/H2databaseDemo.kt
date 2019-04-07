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
    
    //数据库连接URL，当前连接的是目录下的gacl数据库
    val JDBC_URL = "jdbc:h2:/Users/wyd/h2test"
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