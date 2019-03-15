import com.rainday.handler.RelayHandlerKt
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.BlockingVariables
import spock.util.concurrent.PollingConditions
import util.TestUtil

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


    def "sum should return param1+param2"() {
        expect:
        RelayHandlerKt.suma(2, 3) == 5
    }

    def "httpclient ping pong test"() {
        given: "declare a response "
        HttpClientResponse response
        String respStr

        and: "init vertx"
        def vertx = Vertx.vertx(vxOption)
        def httpClient = vertx.createHttpClient(new HttpClientOptions().setKeepAliveTimeout(5))
        def clientRequest = httpClient.getAbs("http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859")

        when: "request http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859"
        clientRequest.handler({
            response = it
            it.handler({
                respStr = it.toString()
            })

        })
        clientRequest.end()
        TestUtil.waitResult(respStr, 1500)
        println respStr
        then: "check the result"
        response.statusCode() == 200
        respStr.contains("河南")
        respStr.contains("商丘")
    }

    def "httpclient ping pong test with pollingConditions"() {
        given: "declare a response "
        def vertx = Vertx.vertx(vxOption)
        def condition = new PollingConditions(timeout: 10)
        HttpClientResponse response
        String respStr

        and: "init vertx"
        def httpClient = vertx.createHttpClient(new HttpClientOptions().setKeepAliveTimeout(5))
        def clientRequest = httpClient.getAbs("http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859")

        when: "request http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859"
        clientRequest.handler({
            response = it
            it.handler({
                respStr = it.toString()
            })

        })
        clientRequest.end()

        then: "check the result"
        condition.within(2){
            response.statusCode() == 200
        }
        condition.eventually {
            println "==== " + respStr
            respStr.contains("商丘")
            respStr.contains("河南")
        }
    }

    def "httpclient ping pong test with blockingvariable or blockingvariables"() {
        given: "declare a response "
        def vertx = Vertx.vertx(vxOption)

        and: "init vertx"
        def httpClient = vertx.createHttpClient(new HttpClientOptions().setKeepAliveTimeout(5))
        def clientRequest = httpClient.getAbs("http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859")

        when: "request http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859"
        def statusCode = new BlockingVariable<Integer>()
        def results = new BlockingVariables()
        clientRequest.handler({
            statusCode.set(it.statusCode())
            it.handler({
                results.setProperty("body",it.toString())
            })

        })
        clientRequest.end()

        then: "check the result"
        println results.getProperty("body")
        statusCode.get() == 200
        results.getProperty("body").toString().contains("河南")
        results["body"].toString().contains("商丘")

    }

    def "the max num using where"() {
        expect: "get max num"
        Math.max(a, b) == c

        where:
        a | b  || c
        1 | 3  || 3
        2 | 99 || 99
        4 | 1  || 1
    }

    def "the max num using when then"() {
        when: "formula"
        def max = Math.max(a, b)
        then: "get max num"
        max == c

        where:
        a | b  || c
        1 | 3  || 3
        2 | 99 || 99
        4 | 1  || 1
    }

    def "the max num using given when then"() {
        given: "give some param"
        def m = 1, n = 4
        when: "formula"
        def max = Math.max(m, n)
        then: "get max num"
        max == 3
    }

}
