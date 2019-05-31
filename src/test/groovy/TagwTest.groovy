import com.rainday.Launcher
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
/**
 * Created by wyd on 2019/3/11 10:52:33.
 */
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


    // run before every feature method
    def setup() {
        //init tagw
        def args = ["run", "com.rainday.BootstrapVerticle", "-conf", "src/main/resources/example-conf.json", "--launcher-class=com.rainday.com.a.Launcher"]
//        def args = "run com.rainday.BootstrapVerticle -conf src/main/resources/example-conf.json --launcher-class=com.rainday.com.a.Launcher"
        Launcher.main(*args)

        //init vertx httpclient


    }

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
        respStr.get() == "pong"
    }
}
