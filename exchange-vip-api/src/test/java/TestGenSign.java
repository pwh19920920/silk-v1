

import com.spark.bitrade.util.SignUtil;
import com.spark.bitrade.util.ThirdDESUtil;

/**
 *
 */
public class TestGenSign {
    public static void main(String[] args) throws Exception {
        String authKey = "123456678sdfsafsd23423";  //准入授权码
        String serkey = String.valueOf(10001); //加密密钥
        long time = 1539338627384L;                 //固定时间戳

        String md5Sign = SignUtil.md5Sign(authKey, "|", time);
        System.out.println("md5Sign=" + md5Sign);

        //String sign = SignUtil.encryptDes(md5Sign, serkey);
        String sign = ThirdDESUtil.ENCRYPTMethod(md5Sign, serkey);
        System.out.println("sign=" + sign);
    }
}
