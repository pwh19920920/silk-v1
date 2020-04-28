

import com.spark.bitrade.util.DESUtil;
import com.spark.bitrade.util.Md5;
import com.spark.bitrade.util.SignUtil;
import com.spark.bitrade.util.ThirdDESUtil;

/**
 *
 */
public class TestSign {
    public static void main(String[] args) {
        String serkey = "123456678sdfsafsd23423";
        String key = String.valueOf(74658);

        try {
            //System.out.println(Md5.md5Digest(serkey));

            /*String md5Sign = signatureMd5(serkey);
            System.out.println(md5Sign);
            String sign = encryptDes(md5Sign, key);
            System.out.println("11="+sign);


            System.out.println(encryptDes(md5Sign, "71638"));

            System.out.println("11="+decryptDes(sign, key));*/


            //String sign1 =SignUtil.md5Sign(serkey, "|", 1538904572123L);
            String sign1 =SignUtil.md5Sign(serkey, "|", System.currentTimeMillis());
            //System.out.println("test="+SignUtil.md5Sign(serkey, "|", System.currentTimeMillis()));
            System.out.println("test="+sign1);
            System.out.println("test="+encryptDes(sign1, key));

            //生成校验码
            String sign2 = SignUtil.sign(serkey, key);
            System.out.println("22="+sign2);
            //System.out.println("22="+SignUtil.decryptDes(sign2, key));

            //校验
            System.out.println(SignUtil.checkSign(serkey, key, sign2));

            String ss ="a03fa344c90e885d5d8391512865d120a69b645f08efb177328c0d9a71ee3163ec3a5809913657ab02035037ad493fd4";
            System.out.println(ThirdDESUtil.decrypt(ss, serkey));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     *  MD5签名 = MA5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
//     * @param serKey  准入授权码
//     * @param separator 分隔符
//     * @param currentTime 系统时间戳
//     * @return
//     * @throws Exception
//     */
//    private static String signatureMd5(String serKey, String separator, long currentTime) throws Exception {
//        StringBuilder source = new StringBuilder();
//        source.append(serKey).append(separator).append(currentTime); // 准入授权码+分隔符+系统时间戳
//
//        //MA5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
//        return new StringBuilder(md5Digest(source.toString())).append(separator).append(currentTime).toString();
//    }
//
//    /**
//     * MD5签名
//     * @param serKey  准入授权码
//     * @return 根据约定进行的签名
//     * @throws Exception
//     */
//    private static String signatureMd5(String serKey) throws Exception {
//        return signatureMd5( serKey, "|", System.currentTimeMillis());
//    }
//
//    //md5
//    private static String md5Digest(String sourceStr) throws Exception {
//        return Md5.md5Digest(sourceStr);
//    }
//
//    /**
//     *  AES加密
//     * @param message
//     * @param key
//     * @return
//     * @throws Exception
//     */
    private static String encryptDes(String message, String key) throws Exception {
        return DESUtil.ENCRYPTMethod(message, key);
    }
//
//    /**
//     *  AES解密
//     * @param message
//     * @param key
//     * @return
//     * @throws Exception
//     */
//    private static String decryptDes(String message, String key) throws Exception {
//        return DESUtil.decrypt(message, key);
//    }
//
//    //接口校验码 = AES(MD5签名, 加密密钥（为用户ID）)
//
//    /**
//     * 生成校验码
//     * @param serKey 准入授权码
//     * @param key4uid 加密密钥（为用户ID）
//     * @return 校验码
//     * @throws Exception
//     */
//    public static String sign(String serKey,String key4uid) throws Exception {
//        String md5Sign = signatureMd5(serKey);
//        return encryptDes(md5Sign, key4uid);
//    }
//
//    /**
//     * 签名校验
//     * @param serKey 准入授权码
//     * @param key4uid  加密密钥（为用户ID）
//     * @param separator 分隔符
//     * @param signMsg 校验码信息
//     * @return
//     * @throws Exception
//     */
//    public static boolean checkSign(String serKey, String key4uid,
//                                    String separator, String signMsg) throws Exception {
//        //MA5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
//        String source = decryptDes(signMsg, key4uid);
//        int index = source.indexOf(separator);
//        System.out.println("ss="+source);
//        if(index == -1){
//            return false;
//        }
//        //获取校验码生成的时间
//        String time = source.substring(index+separator.length());
//
//        //时间差比较，允许的时间差为2分钟 TODO
//
//        //还原MD5签名
//        String targetMd5Sign = signatureMd5(serKey, separator, Long.parseLong(time));
//        System.out.println("ss="+targetMd5Sign);
//        if(!targetMd5Sign.equalsIgnoreCase(source)) {
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * 签名校验
//     * @param serKey 准入授权码
//     * @param key4uid  加密密钥（为用户ID）
//     * @param signMsg 校验码信息
//     * @return
//     * @throws Exception
//     */
//    public static boolean checkSign(String serKey, String key4uid,
//                                    String signMsg) throws Exception {
//        return checkSign(serKey, key4uid, "|", signMsg);
//    }

}
