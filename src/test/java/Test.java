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

}
