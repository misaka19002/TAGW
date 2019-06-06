import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * Created by wyd on 2019/2/28 15:41:22.
 */
public class Test {

    @org.junit.Test
    public void enc_dec(){
        String jasyPwd = "rainday@rainday";
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(jasyPwd);
        String encPwd = textEncryptor.encrypt("123456");
        System.out.println(encPwd);
        System.out.println(textEncryptor.decrypt(encPwd));

        StandardPBEStringEncryptor pbeStringEncryptor = new StandardPBEStringEncryptor();
        pbeStringEncryptor.setPassword(jasyPwd);
        System.out.println(pbeStringEncryptor.encrypt("123456"));
        System.out.println(pbeStringEncryptor.decrypt(pbeStringEncryptor.encrypt("123456")));
    }

    @org.junit.Test
    public void genParampairs() {
        /*{
            "inName": "pid",
            "inType": "path",
            "outName": "pid",
            "outType": "path"
        }*/
        JsonArray array = new JsonArray();
        String[] types = {"path", "query", "header", "body"};
        for (int i = 0; i < types.length; i++) {
            for (int i1 = 0; i1 < types.length; i1++) {
                JsonObject json = new JsonObject()
                    .put("inName", types[i] + "-" + types[i1])
                    .put("inType", types[i])
                    .put("outName", types[i1] + "-" + types[i])
                    .put("outType",types[i1]);
                array.add(json);
                
            }
        }
        System.out.println(array.toString());
    }
}
