package com.rainday

import com.rainday.ext.toJsonString
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * Created by wyd on 2019/2/28 15:40:53.
 *
 */
class Launcher : VertxCommandLauncher(), VertxLifecycleHooks {
    private val DEFAULT_DNS0 = "114.114.114.114"
    private val DEFAULT_DNS1 = "114.114.115.115"
    private val DEFAULT_DNS2 = "223.5.5.5"
    private val DEFAULT_DNS3 = "223.6.6.6"

    /**
     * Main entry point.
     *
     * @param args the user command line arguments.
     */
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("jvm starting ...")
            //netty Force to use slf4j
            InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
            //vertx Force to use slf4j
            System.setProperty(
                "vertx.logger-delegate-factory-class-name",
                "io.vertx.core.logging.Log4j2LogDelegateFactory"
                //"io.vertx.core.logging.SLF4JLogDelegateFactory"
            )
            //关闭dns
//            System.setProperty("vertx.disableDnsResolver", "true")
            Launcher().dispatch(args)
        }
    }

    /**
     * Hook for sub-classes of [Launcher] after the config has been parsed.
     *
     * @param config the read config, empty if none are provided.
     */
    override fun afterConfigParsed(config: JsonObject) {}

    /**
     * Hook for sub-classes of [Launcher] before the vertx instance is started.
     *
     * @param options the configured Vert.x options. Modify them to customize the Vert.x instance.
     */
    override fun beforeStartingVertx(options: VertxOptions) {
        val addrResolver = AddressResolverOptions()
            .setQueryTimeout(1000)
            .addServer(DEFAULT_DNS0)
            .addServer(DEFAULT_DNS1)
            .addServer(DEFAULT_DNS2)
            .addServer(DEFAULT_DNS3)
        options.addressResolverOptions = addrResolver
        println(Json.encode(options))
    }

    /**
     * Hook for sub-classes of [Launcher] after the vertx instance is started.
     *
     * @param vertx the created Vert.x instance
     */
    override fun afterStartingVertx(vertx: Vertx) {
    }

    /**
     * Hook for sub-classes of [Launcher] before the verticle is deployed.
     *
     * @param deploymentOptions the current deployment options. Modify them to customize the deployment.
     */
    override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions) {

    }

    override fun beforeStoppingVertx(vertx: Vertx) {

    }

    override fun afterStoppingVertx() {

    }

    /**
     * A deployment failure has been encountered. You can override this method to customize the behavior.
     * By default it closes the `vertx` instance.
     *
     * @param vertx             the vert.x instance
     * @param mainVerticle      the verticle
     * @param deploymentOptions the verticle deployment options
     * @param cause             the cause of the failure
     */
    override fun handleDeployFailed(vertx: Vertx, mainVerticle: String, deploymentOptions: DeploymentOptions, cause: Throwable) {
        // Default behaviour is to close Vert.x if the deploy failed
        vertx.close()
    }
}