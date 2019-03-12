import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;

import java.util.Date;

/**
 * Created by wyd on 2019/3/12 15:14:25.
 */
public class HttpclientExample {

    public static void main(String[] args) throws Exception {
        new Thread(() -> {

            Vertx vertx = Vertx.vertx();
            HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions().setIdleTimeout(5));
//            HttpClientRequest clientRequest = httpClient.getAbs("https://www.jianshu.com/p/a4be10076a6e");
            HttpClientRequest clientRequest = httpClient.getAbs("http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=15993978859");
            clientRequest.handler(response -> {
                response.handler(x -> {
                    System.out.println(x.toString());
                });
            });
            clientRequest.exceptionHandler(e -> {
                e.printStackTrace();
            });
            clientRequest.endHandler(x -> {
                    System.out.println("connection is closed1"+new Date());
                clientRequest.connection().closeHandler(dfds -> {
                    System.out.println("connection is closed0"+new Date());
                });
                System.out.println("clientrequest end");
            });

            clientRequest.end();
        }).start();
    }
}
