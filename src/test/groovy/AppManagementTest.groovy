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
}
