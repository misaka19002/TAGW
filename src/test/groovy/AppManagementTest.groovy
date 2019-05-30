import com.rainday.BootstrapVerticle
import com.rainday.Launcher
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

import javax.ws.rs.core.MediaType
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class AppManagementTest extends Specification {

    def "tagw start up"() {
        //[--launcher-class=com.rainday.com.a.Launcher, -conf, com.rainday.BootstrapVerticle, run, src/main/resources/example-conf.json]
        given: "set param to start up"
        def args = Arrays.asList(" --launcher-class=com.rainday.com.a.Launcher", " -conf", " com.rainday.BootstrapVerticle", " run", "src/main/resources/example-conf.json")
        when: "start"
        Launcher.main(args)
        then: ""
        1 == 1

    }

    def "timeTest"() {
        //[--launcher-class=com.rainday.com.a.Launcher, -conf, com.rainday.BootstrapVerticle, run, src/main/resources/example-conf.json]
        given: "set param to start up"
        def dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        when: "start"
        def r = Period.between(LocalDate.parse(from, dtf), LocalDate.parse(to, dtf)).getMonths()

        then:""
        println r
        r == exp
        where: "from to "
        from         | to           | exp
        "2019-02-20" | "2019-03-19" | 0
        "2019-02-20" | "2019-03-20" | 1
        "2019-02-20" | "2019-03-21" | 1
        "2019-02-20" | "2019-03-22" | 1
        "2019-02-20" | "2019-05-19" | 3
        "2019-02-20" | "2019-05-19" | 3
        "2019-02-20" | "2019-05-20" | 4

    }

    def "vertx up -> add an app"() {
        given: "declare variable"
        BlockingVariable<Boolean> status = new BlockingVariable<>(10)
        BlockingVariable<Integer> respStatus = new BlockingVariable<>(10)
        BlockingVariable<String> resp = new BlockingVariable<>(10)
        BlockingVariable<MultiMap> respHeaders = new BlockingVariable<>(10)

        def appInfo = "{\n" +
            "  \"appName\": \"app1\",\n" +
            "  \"port\": 8081,\n" +
            "  \"description\": \"这是一个测试的app\",\n" +
            "  \"status\": \"active\",\n" +
            "  \"relays\": [\n" +
            "    {\n" +
            "      \"inUrl\": \"/relay/jianshu/1/:pid\",\n" +
            "      \"inMethod\": \"GET\",\n" +
            "      \"outUrl\": \"https://www.jianshu.com/p/:pid\",\n" +
            "      \"outMethod\": \"GET\",\n" +
            "      \"transmission\": true,\n" +
            "      \"status\": \"active\",\n" +
            "      \"paramPairs\": [\n" +
            "        {\n" +
            "          \"inName\": \"pid\",\n" +
            "          \"inType\": \"path\",\n" +
            "          \"outName\": \"pid\",\n" +
            "          \"outType\": \"path\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"inUrl\": \"/relay/jianshu/2/:pid\",\n" +
            "      \"inMethod\": \"GET\",\n" +
            "      \"outUrl\": \"https://www.jianshu.com/p/:pid\",\n" +
            "      \"outMethod\": \"GET\",\n" +
            "      \"transmission\": true,\n" +
            "      \"status\": \"active\",\n" +
            "      \"paramPairs\": [\n" +
            "        {\n" +
            "          \"inName\": \"pid\",\n" +
            "          \"inType\": \"path\",\n" +
            "          \"outName\": \"pid\",\n" +
            "          \"outType\": \"path\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}"

        and: "init "
        def vertx = Vertx.vertx()
        vertx.deployVerticle(BootstrapVerticle.class.name) {
            if (it.succeeded()) {
                println "vertx up  " + it.result()
                status.set(true)
            } else {
                println "vertx up failed " + it.cause().message
            }
        }
        when: "add an app to this vertx instance"
        status.get()//waiting for verticle start up
        //httpclient add app
        def clientRequest = vertx.createHttpClient().postAbs("http://localhost:8080/apps")
        clientRequest.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        clientRequest.handler({
            respStatus.set(it.statusCode())
            respHeaders.set(it.headers())


            it.bodyHandler({
                resp.set(it.toString())
            })
            /*it.endHandler({
                println "endHandler ds"
                resp.set("")
            })*/
        })
        clientRequest.setChunked(true).write(appInfo).end()

        then: "check the result"
        respStatus.get() == 201
        respHeaders.get().toList().toString().contains("location")
    }

}
