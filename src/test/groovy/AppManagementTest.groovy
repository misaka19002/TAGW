import io.vertx.core.json.JsonObject
import spock.lang.Specification

class AppManagementTest extends Specification {

    def "tagw start up"() {
        //[--launcher-class=com.rainday.Launcher, -conf, com.rainday.BootstrapVerticle, run, src/main/resources/example-conf.json]
        given: "set param to start up"
        def args = Arrays.asList(" --launcher-class=com.rainday.Launcher", " -conf", " com.rainday.BootstrapVerticle", " run", "src/main/resources/example-conf.json")
        when: "start"
        Launcher().main(args)
        then: ""
        1==1

    }

    def "sfsdf"() {
        given: ""
        def json = new JsonObject()
        when: "sdf"
        def d = json.getJsonArray("aa")
        println d
        then: "check"
        d == null

    }
}
