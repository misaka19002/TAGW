package com

import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.spi.resolver.ResolverProvider
import java.util.*


/**
 *
 * @author wyd
 * @time  2019-03-22 21:15
 * @description
 */
fun main(args: Array<String>) {
    //netty Force to use slf4j
    InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    //vertx Force to use slf4j
    System.setProperty(
        "vertx.logger-delegate-factory-class-name",
        "io.vertx.core.logging.Log4j2LogDelegateFactory"
        //"io.vertx.core.logging.SLF4JLogDelegateFactory"
    )
    //关闭dns
//    System.setProperty("vertx.disableDnsResolver", "true")
//    System.setProperty(ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME, "true")
    //设置 vertx dns server
    val addrResolver = AddressResolverOptions()
        .setQueryTimeout(500)
        .addServer("114.114.114.114")
    val vxOptions = VertxOptions().setAddressResolverOptions(addrResolver)

    val vertx = Vertx.vertx(vxOptions)
    println("11111")
    val clientRequest =
        vertx.createHttpClient().getAbs("https://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859")

    clientRequest.handler {
        it.bodyHandler {
            println("dsfsdf")
            println(it.toString())
        }
    }
    clientRequest.exceptionHandler {
        println("请求异常 ${it.message}")
    }
    clientRequest.end()
}