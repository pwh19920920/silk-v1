import com.spark.bitrade.util.DESUtil;
import com.spark.bitrade.util.SignUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import java.nio.charset.Charset;

/***
 * 接口签名测试
 * @author yangch
 * @time 2018.10.12 16:08
 */
public class SignTest {

    public static void main(String[] args) throws Exception {
        String authKey = "123456678sdfsafsd23423";  //准入授权码
        String serkey = String.valueOf(10001234); //加密密钥（为用户ID）

        //内部接口
        String s1 = DESUtil.ENCRYPTMethod(authKey, serkey);
        System.out.println(s1);

        //---------------
        //加密
        String encryptResult = DesUtil.encrypt(authKey, serkey);
        System.out.println(encryptResult);

        //解码
        String decryResult = DesUtil.decrypt(encryptResult, serkey);
        System.out.println(decryResult);
        System.out.println();

    }

    public static void test1(){
        String authKey = "123456678sdfsafsd23423";  //准入授权码
        String serkey = String.valueOf(10001); //加密密钥（为用户ID）
        try {
            //生成校验码
            String sign = SignUtil.sign(authKey, serkey);
            System.out.println(sign);
            System.out.println("");

            //校验校验码
            System.out.println(SignUtil.checkSign(authKey, serkey, sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class Des {
        /**
         * 加密
         * @param data
         * @param sKey
         * @return
         */
        public static byte[] encrypt(byte[] data, String sKey) {
            try {
                byte[] key = sKey.getBytes();
                // 初始化向量
                IvParameterSpec iv = new IvParameterSpec(key);
                DESKeySpec desKey = new DESKeySpec(key);
                // 创建一个密匙工厂，然后用它把DESKeySpec转换成securekey
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                SecretKey securekey = keyFactory.generateSecret(desKey);
                // Cipher对象实际完成加密操作
                Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                // 用密匙初始化Cipher对象
                cipher.init(Cipher.ENCRYPT_MODE, securekey, iv);
                // 现在，获取数据并加密
                // 正式执行加密操作
                return cipher.doFinal(data);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 解密
         * @param src
         * @param sKey
         * @return
         * @throws Exception
         */
        public static byte[] decrypt(byte[] src, String sKey) throws Exception {
            byte[] key = sKey.getBytes();
            // 初始化向量
            IvParameterSpec iv = new IvParameterSpec(key);
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(key);
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, iv);
            // 真正开始解密操作
            return cipher.doFinal(src);
        }

        /**
         * 将二进制转换成16进制
         *
         * @param buf
         * @return
         */
        public static String parseByte2HexStr(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        }

        /**
         * 将16进制转换为二进制
         *
         * @param hexStr
         * @return
         */
        public static byte[] parseHexStr2Byte(String hexStr) {
            if (hexStr.length() < 1) return null;
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }
    }


    private static class DesUtil {
        private static final Charset CHARSET = Charset.forName("gb2312");

        /**
         * 加密
         * @param srcStr
         * @param charset
         * @param sKey
         * @return
         */
        public static String encrypt(String srcStr, Charset charset, String sKey) {
            byte[] src = srcStr.getBytes(charset);
            byte[] buf = Des.encrypt(src, sKey);
            return Des.parseByte2HexStr(buf);
        }

        /**
         * 加密
         * @param srcStr
         * @param sKey
         * @return
         */
        public static String encrypt(String srcStr, String sKey) {
            return encrypt( srcStr, CHARSET, sKey);
        }


        /**
         * 解密
         *
         * @param hexStr
         * @param sKey
         * @return
         * @throws Exception
         */
        public static String decrypt(String hexStr, Charset charset, String sKey) throws Exception {
            byte[] src = Des.parseHexStr2Byte(hexStr);
            byte[] buf = Des.decrypt(src, sKey);
            return new String(buf, charset);
        }
        public static String decrypt(String hexStr, String sKey) throws Exception {
            return decrypt( hexStr,  CHARSET, sKey);
        }
    }
}
