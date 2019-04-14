package com

import com.rainday.ext.toJsonString
import com.zaxxer.hikari.HikariDataSource
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType


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
//    val DRIVER_CLASS = "org.h2.Driver"

    val dataSource = HikariDataSource().apply {
        jdbcUrl = JDBC_URL
        username = USERNAME
        password = PASSWORD
        connectionTimeout = 60000
        idleTimeout = 60000
    }
//    val dsl = DSL.using(dataSource, SQLDialect.H2)
    //jooq bug 对于h2支持，createtable是大写表名，小写表名应该是一样的。但是创建错了
//    dsl.createTable("AA").column("aa", SQLDataType.VARCHAR(20)).execute()
//    dsl.createTable("aa").column("aa", SQLDataType.VARCHAR(20)).execute()



    val dsl = DSL.using(dataSource, SQLDialect.H2)
    dsl.dropTableIfExists("AA").execute()
    dsl.createTable("AA").column("aa", SQLDataType.VARCHAR(20)).execute()
    val r1 = DSL.using(dataSource, SQLDialect.H2).selectFrom<Record>("aa").fetchMaps()
    val r11 = DSL.using(dataSource, SQLDialect.H2).selectFrom<Record>("AA").fetchMaps()
    val r2 = DSL.using(dataSource, SQLDialect.H2).fetch("select * from USER_INFO").intoMaps()
    println(r1.toJsonString())
    println(r11.toJsonString())
    println(r2.toJsonString())

//    dsl.close()

    /*
    使用h2自带的DataSource
    val dataSource = JdbcDataSource()
     dataSource.setUrl(JDBC_URL)
     dataSource.user = USERNAME
     dataSource.password = PASSWORD

     DSL.using(dataSource,SQLDialect.H2).xxxxx*/
}