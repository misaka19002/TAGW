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
            it.bodyHandler({
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
            it.bodyHandler({
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
            it.bodyHandler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end(appConfig)
        def appkey = new JsonObject(respStr.get().toString()).getString("appKey")

        then: "check the result"
        appkey == "appKey001"
    }

    //deploy an app then get the routes
    def "relay transmission param shift"() {
        given: "declare a response "
        HttpClientResponse response
        def respStr = new BlockingVariable(5)

        and: "init clientRequest"
        def url = "http://127.0.0.1:8081/relay/path_aaaa/path_bbbb/path_cccc/path_dddd" + "?query_path=query_aaaa&query_query=query_bbbb&query_header=query_cccc&query_body=query_dddd"
        def clientRequest = httpClient.postAbs(url)
        clientRequest.putHeader("header_path", "header_aaaa")
        clientRequest.putHeader("header_query", "header_bbbb")
        clientRequest.putHeader("header_header", "header_cccc")
        clientRequest.putHeader("header_body", "header_dddd")
        def bodyParam = new JsonObject([
            "body_path"  : "body_aaaa",
            "body_query" : "body_bbbb",
            "body_header": "body_cccc",
            "body_body"  : "body_dddd",
        ]).toString()

        when: "fire and get"
        clientRequest.handler({
            response = it
            it.handler({
                respStr.set(it.toString())
            })
        })
        clientRequest.end(bodyParam)
        then: "check the result"
        def json = new JsonObject(respStr.get().toString())

        //assert path varuable
        json.getString("path").contains("path_aaaa")
        json.getString("path").contains("query_aaaa")
        json.getString("path").contains("header_aaaa")
        json.getString("path").contains("body_aaaa")
        //assert query varuable
        json.getString("query").contains("path_bbbb")
        json.getString("query").contains("query_bbbb")
        json.getString("query").contains("header_bbbb")
        json.getString("query").contains("body_bbbb")
        //assert header varuable
        json.getString("header").contains("path_cccc")
        json.getString("header").contains("query_cccc")
        json.getString("header").contains("header_cccc")
        json.getString("header").contains("body_cccc")
        //assert body varuable
        json.getString("body").contains("path_dddd")
        json.getString("body").contains("query_dddd")
        json.getString("body").contains("header_dddd")
        json.getString("body").contains("body_dddd")
    }
}
