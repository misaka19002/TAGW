import com.rainday.Launcher
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.json.JsonObject
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.BlockingVariable

import javax.ws.rs.core.MediaType

/**
 * Created by wyd on 2019/3/11 10:52:33.
 */
@Stepwise
class TagwTest extends Specification {
    //设置dns为8.8.8.8
    def addressResolverOptions = new AddressResolverOptions()
        .setNdots(1)
        .setServers(Arrays.asList("114.114.114.114"))
//    .setServers(Arrays.asList("8.8.8.8"))
//    .setSearchDomains(Arrays.asList(".com"))
    def vxOption = new VertxOptions().setAddressResolverOptions(addressResolverOptions)
    def vertx = Vertx.vertx(vxOption)
    def httpClient = vertx.createHttpClient(new HttpClientOptions().setKeepAliveTimeout(5))


//    def setup() {}          // run before every feature method
//    def cleanup() {}        // run after every feature method
//    def setupSpec() {}     // run before the first feature method
//    def cleanupSpec() {}   // run after the last feature method
    // run before every feature method
    def setupSpec() {

        println "sssssssssssssssss"
        //init tagw
        def args = ["run", "com.rainday.BootstrapVerticle", "-conf", "src/main/resources/example-conf.json", "--launcher-class=com.rainday.com.a.Launcher"]
        Launcher.main(*args)

        sleep(2000)

        //init vertx httpclient
    }

    //ping pong test
    def "bootstrapVerticle startup test"() {
        given: "declare a response "
        HttpClientResponse response
        def respStr = new BlockingVariable(5)

        and: "init clientRequest"
        def clientRequest = httpClient.getAbs("http://127.0.0.1:8080/ping")

        when: "fire and get"
        clientRequest.handler({
            response = it
            it.handler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end()
        then: "check the result"
        println respStr.get()
        respStr.get() == "pong"
    }

    def "truncate database"() {
        given: "sdfsd"
        def respStr = new BlockingVariable(50)
        def clientRequest = httpClient.getAbs("http://127.0.0.1:8080/truncate")

        when: "fire and get"
        clientRequest.handler({
            it.handler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end()
        then: "check the result"
        respStr.get() == "success"

    }

    //deploy an app then get the routes
    def "deploy app"() {
        given: "declare a response "
        HttpClientResponse response
        def respStr = new BlockingVariable(5)

        and: "init clientRequest"
        def clientRequest = httpClient.postAbs("http://127.0.0.1:8080/apps")
        clientRequest.putHeader("content-type", MediaType.APPLICATION_JSON)

        when: "fire and get"
        def is = this.getClass().getResourceAsStream("app-add.json")
        def appConfig = new InputStreamReader(is).readLines().join()
        clientRequest.handler({
            response = it
            it.handler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end(appConfig)

        then: "check the result"
        println respStr.get()
        respStr.get() == "pong"
    }

    //deploy an app then get the routes
    def "relay transmission param shift"() {
        given: "declare a response "
        HttpClientResponse response
        def respStr = new BlockingVariable(5)

        and: "init clientRequest"
        def url = "http://127.0.0.1:8081/relay/path-aaaa/path-bbbb/path-cccc/path-dddd"
        url.concat("?query-path=query-aaaa&query-query=query-bbbb&query-header=query-cccc&query-body=query-dddd")
        def clientRequest = httpClient.postAbs("")
        clientRequest.putHeader("header-path", "header-aaaa")
        clientRequest.putHeader("header-query", "header-bbbb")
        clientRequest.putHeader("header-header", "header-cccc")
        clientRequest.putHeader("header-body", "header-dddd")
        clientRequest.write(new JsonObject([
            "body-path"  : "body-aaaa",
            "body-query" : "body-bbbb",
            "body-header": "body-cccc",
            "body-body"  : "body-dddd",
        ]).toString())

        when: "fire and get"
        clientRequest.handler({
            response = it
            it.handler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end(appConfig)

        then: "check the result"
        println respStr.get()
        println respStr.get()
        respStr.get() == "pong"
    }
}
