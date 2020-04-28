

import com.spark.bitrade.util.SignUtil;

/**
 *
 */
public class TestCheckSign {
    public static void main(String[] args) throws Exception {
        String authKey = "123456678sdfsafsd23423";  //准入授权码
        String serkey = String.valueOf(10001); //加密密钥

        String message = "92bd9dc362bc65734ff2b18e352714ed06dc2cd7b7fda12859b835b974a88f6f8fdb33d94200c1c85b1e559a1247eb91";

        System.out.println("校验结果="+SignUtil.checkSign(authKey, serkey, message));

    }
}
