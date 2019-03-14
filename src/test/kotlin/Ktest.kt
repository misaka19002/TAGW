import com.rainday.model.Application
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.junit.Test

/**
 * Created by wyd on 2019/2/28 15:41:46.
 */

class Ktest {
    @Test
    fun json2beanTest() {

        val json = JsonObject(
            "{\n" +
                    "  \"appName\": \"testapp1\",\n" +
                    "  \"port\": 8081,\n" +
                    "  \"description\": \"hahhahahahhahahahahahdfjhberfkjhk哈\"\n" +
                    "}"
        )

        Application(appName = ",", port = 33, description = "sd", deployId = "")

        Json.mapper.readValue<Application>(json.toString(), Application::class.java)


    }
}



