package com.rainday

import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/2/28 15:40:53.
 *
 */
class Launcher : VertxCommandLauncher(), VertxLifecycleHooks {
    override fun handleDeployFailed(
        vertx: Vertx?,
        mainVerticle: String?,
        deploymentOptions: DeploymentOptions?,
        cause: Throwable?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun beforeStartingVertx(options: VertxOptions?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterStoppingVertx() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterConfigParsed(config: JsonObject?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterStartingVertx(vertx: Vertx?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun beforeStoppingVertx(vertx: Vertx?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //netty Force logging to Log4j
            InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
            //vertx Force to use slf4j
            System.setProperty(
                "vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.Log4j2LogDelegateFactory"
//                "io.vertx.core.logging.SLF4JLogDelegateFactory"
            )
            System.setProperty("vertx.disableDnsResolver", "true")
            Launcher().dispatch(args)
        }
    }
}