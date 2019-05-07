package com

import org.h2.jdbcx.JdbcDataSource
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

    val dataSource = JdbcDataSource().apply {
        setUrl(JDBC_URL)
        user = USERNAME
        password = PASSWORD
    }
    val dsl = DSL.using(dataSource, SQLDialect.H2)
    //jooq bug 对于h2支持，createtable是大写表名，小写表名应该是一样的。但是创建错了
    dsl.dropTableIfExists("AA").execute()
    dsl.createTable("AA").column("aa",SQLDataType.VARCHAR(30)).execute()

    val r1 = dsl.selectFrom<Record>("aa").fetchMaps()
    val r2 = dsl.fetch("select * from aa").intoMaps()
    val r3 = dsl.selectCount().from("aa").fetch().intoMaps()
    val r4 = dsl.selectOne().from("aa").fetch().intoMaps()

    val r1s = dsl.selectFrom<Record>("aa").sql
    val r2s = dsl.selectCount().from("aa").where("1=1").sql
    val r3s = dsl.select(DSL.count(DSL.field("2"))).from("aa").getSQL()
    Thread.sleep(1000)
}