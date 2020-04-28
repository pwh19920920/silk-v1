import com.spark.bitrade.util.MD5Util;
import com.spark.bitrade.utils.Base64Util;
import com.spark.bitrade.utils.DesBase64Util;
import com.spark.bitrade.utils.RSAFullUtils;
import com.spark.bitrade.utils.RSAUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.tomcat.util.security.MD5Encoder;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SimpleTest {

    @Test
    public void testDate(){
        String pssw = DigestUtils.md5Hex("111111hello, moto");
        String pass = new SimpleHash("md5", pssw, "313434303436323537393139333635313230", 2).toHex().toLowerCase();
        System.out.println(pass);
    }

    @Test
    public void testBigDecimal(){
        String encodeStr = DesBase64Util.encodeDES("欢迎来到chacuo.net","zzsc2019");
        System.out.println(encodeStr);
        System.out.println(DesBase64Util.decodeDES(encodeStr,"zzsc2019"));
    }

    @Test
    public void testRSA(){

        /**
         * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAioTG6cc8qCBEgAxcx9Rp0uOorB/C3Tku+barbhOrgWNM63u0wY849RYFT1RAocc4VktY+/yF/dS7+NPntmeaW144tpaaW9HWSBIPUiHLINgdr5lf1gVje3G5mVH5S37UhtuDZ8d6fMddSjcSCk2jgoaaB8HzRYYO9opt6b02DLVyIY87iE0AvE4umSM24NSzOpfmzbMOAqGUJxr7dx+suzqlUDE0G3VpbZgE0eb0h+bkZle225W+90MSDFB3HRJ+LrdAGn1As2z6fl53XMczeen3PsPTUAqxLJJNL6UaghRdtdiDymCOtYxuaQSuahhP4q+d0qz/hxuGBhluSMoVZQIDAQAB
         */

        /**
         * MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCKhMbpxzyoIESADFzH1GnS46isH8LdOS75tqtuE6uBY0zre7TBjzj1FgVPVEChxzhWS1j7/IX91Lv40+e2Z5pbXji2lppb0dZIEg9SIcsg2B2vmV/WBWN7cbmZUflLftSG24Nnx3p8x11KNxIKTaOChpoHwfNFhg72im3pvTYMtXIhjzuITQC8Ti6ZIzbg1LM6l+bNsw4CoZQnGvt3H6y7OqVQMTQbdWltmATR5vSH5uRmV7bblb73QxIMUHcdEn4ut0AafUCzbPp+XndcxzN56fc+w9NQCrEskk0vpRqCFF212IPKYI61jG5pBK5qGE/ir53SrP+HG4YGGW5IyhVlAgMBAAECggEAFmD1Mfe6WP89keJtautqXWPicEyBUvRWHongkFwlLTY5yyGlRlA5R39TREm8V2a0N1zc93kIvMWNyNmTaei7C1/hkb4rGgKAuAwS5ZYIFzpTMXd6AD3GoL7y2U5hFE1dUJxOUonrxBUBgWf8RiXc8M2GPRKclvLdn/8lWsny3YKEdz7bUOtBzWE0VP8s28cl69zIsMG5qQvyxRehPZNg7j0Csszp6KXVUTYLUepdR4sY/wPpHucDSTBHuLoajahQHul7oyu2T198jte4sm8sC/5edydcp8SezhQf8fTcA0ymlFp3uckX41KuPvor/QIH/OGSJXKLFzmQmHek1vj1AQKBgQDtylQ9r/mWXtaUe7DmxUD5ofCl2j7BSuO7hIiru+5bVfOzYmTcxFvayoAgM80Y4906Y1nPvjg7WA/wg5j1uKeQxWUcJMQ1ZwsVTlD0O4b/Gwf+ELp58Gj+V+e5FIJy2Mqk/4RNqg+zNarp4j33dajps+VHY7EJeJfnjqIFFm6EtQKBgQCVIFBNo44N/RFucbIZS+77MRrQDKNXMUVxUfOMcCDQ69rkx8ydOeYO5ueeyqUlAADA4bNVZYKZYKMBmCWVIKYrbGB9NdwEvnXqpOAqMsJSFO90KYskltL1yaq5GMt3+lFRAuaJY+YLpxWbspPFlA9WvCXBU068FSvniGKuxFnr8QKBgQCwnsXvg9PY0tyDMVR2NDFMufHq3q4aGCwKzihNlqs2gCMhLIZKTDcFzb9ZK+C9ChL6GW1OFrXhrkk4liZ47QzZRSUSwGRUXhMnYiJTJhvhRAyts3Muu1jhAh4FpKw37bqmz3tPzG8Y51Xpnrf0JBqwa4RzgCKxLDd4MMM7ECxKuQKBgHdWBY0rzjj5hGKKj4hY0KSpVhiZlHjpD/YwK6L98/TcWS3tiZtkQus38rCK3/8s8m1n8FftRMayo59Z/vFI0FE9iDWKzUfqlngwesaqGgEPidO+jv+3xpg0Su+WnPSlFz+p/4yAqg43Jj6TeFeLNX8mT1xW61Ht61V8OeQ7cu4xAoGAKf9hs8FGg0wjhpt4lCZNwaZUZydfYCeHvEy5f7azfQgg4pW7nnbAc0P+qrU1M2Ho2Hv3Mjpz81BnhVSqo8SGlt0N0z/osWpVfZfEMCOkA+gG2FFLpAC16rfeaOTSRubrPA2kZf9d2Svuf1ak+L6HaH/p78TarPVIx4g8QMxuhS4=
         */
        KeyPair keyPair = RSAUtils.generateKeyPair();
        String privateKey = RSAUtils.getBase64PrivateKey(keyPair.getPrivate());
        String publicKey = RSAUtils.getBase64PublicKey(keyPair.getPublic());

        System.out.println("privateKey: "+privateKey);
        System.out.println("publicKey: "+publicKey);

        String enBase = RSAUtils.encryptAsString("你好",publicKey);

        String content = RSAUtils.decrypt(enBase,privateKey);
        System.out.println("content: "+content);


        String sign = RSAFullUtils.sign(enBase,privateKey);

        Boolean aBoolean = RSAFullUtils.doCheck(enBase,sign,publicKey);

        System.out.println(aBoolean);
    }
}
