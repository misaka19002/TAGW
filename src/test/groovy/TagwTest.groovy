import com.rainday.handler.RelayHandlerKt
import spock.lang.Specification
/**
 * Created by wyd on 2019/3/11 10:52:33.
 */
class TagwTest extends Specification {

    def "sum should return param1+param2"() {
        expect:
        RelayHandlerKt.suma(2, 3) == 5
    }

}
