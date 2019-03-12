import com.rainday.handler.RelayHandlerKt
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import spock.lang.Specification
import util.TestUtil

/**
 * Created by wyd on 2019/3/11 10:52:33.
 */
class TagwTest extends Specification {

    def "sum should return param1+param2"() {
        expect:
        RelayHandlerKt.suma(2, 3) == 5
    }

    def "httpclient ping pong test"() {
        given: "declare a response "
        HttpClientResponse response
        String respStr
        and: "init vertx"
        def vertx = Vertx.vertx()
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
