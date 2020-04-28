package com.spark.bitrade.util;

import javax.crypto.BadPaddingException;

/***
 * 签名工具类
 * @author yangch
 * @time 2018.10.07 12:42
 */
public class SignUtil {
    private final static String defaultSeparator = "|"; //分隔符
    private final static long defaultDiffTime = 2*60; //时间差：单位秒

    //md5
    public static String md5Digest(String sourceStr) throws Exception {
        return Md5.md5Digest(sourceStr);
    }

    /**
     *  AES加密
     * @param message
     * @param serkey
     * @return
     * @throws Exception
     */
    private static String encryptDes(String message, String serkey) throws Exception {
        return ThirdDESUtil.ENCRYPTMethod(message, serkey);
    }

    /**
     *  AES解密
     * @param message
     * @param serkey
     * @return
     * @throws Exception
     */
    private static String decryptDes(String message, String serkey) throws Exception {
        return ThirdDESUtil.decrypt(message, serkey);
    }

    /**
     * 校验时间差
     * @param time1 时间戳1
     * @param time2 时间戳2
     * @return
     */
    private static boolean checkTime(long time1, long time2) {
        long time = Math.abs((time2 - time1)/1000);
        if(defaultDiffTime - time > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  MD5签名
     * @param authKey  准入授权码
     * @param separator 分隔符
     * @param currentTime 系统时间戳
     * @return  MD5签名 = MD5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
     * @throws Exception
     */
    public static String md5Sign(String authKey, String separator, long currentTime) throws Exception {
        StringBuilder source = new StringBuilder();
        source.append(authKey).append(separator).append(currentTime); // 准入授权码+分隔符+系统时间戳

        //MA5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
        return new StringBuilder(md5Digest(source.toString()))
                .append(separator).append(currentTime).toString();
    }

    /**
     * MD5签名
     * @param authKey  准入授权码
     * @return MD5签名信息 = MD5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
     * @throws Exception
     */
    public static String md5Sign(String authKey) throws Exception {
        return md5Sign( authKey, defaultSeparator, System.currentTimeMillis());
    }

    /**
     * 生成校验码
     * @param authKey 准入授权码
     * @param serkey 加密密钥
     * @return 校验码
     * @throws Exception
     */
    public static String sign(String authKey,String serkey) throws Exception {
        //校验码 = AES(MD5签名, 加密密钥)
        String md5Sign = md5Sign(authKey);
        return encryptDes(md5Sign, serkey);
    }

    /**
     * 签名校验
     * @param authKey 准入授权码
     * @param serkey  解密密钥
     * @param separator 分隔符
     * @param signMsg 校验码信息
     * @return 0=校验通过，1=校验码格式有误，2=时间戳校验失败，3=校验码校验失败，4=无效的校验码
     * @throws Exception
     */
    public static int checkSign(String authKey, String serkey,
                                    String separator, String signMsg) throws Exception {
        try {
            //MA5(准入授权码+分隔符+系统时间戳) +分隔符+系统时间戳
            String md5SignSource = decryptDes(signMsg, serkey);
            int index = md5SignSource.indexOf(separator);
            if (index == -1) {
                return 1;
            }
            //获取校验码生成的时间
            long time = Long.parseLong(md5SignSource.substring(index + separator.length()));

            //时间差比较，允许的时间差为2分钟
            if (!checkTime(time, System.currentTimeMillis())) {
                return 2;
            }

            //还原MD5签名
            String md5SignTarget = md5Sign(authKey, separator, time);
            if (!md5SignTarget.equalsIgnoreCase(md5SignSource)) {
                return 3;
            }
        }catch (BadPaddingException ex){
            return 4;
        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        }

        return 0;
    }

    /**
     * 签名校验
     * @param authKey 准入授权码
     * @param serkey  解密密钥
     * @param signMsg 校验码信息
     * @return 0=校验通过，1=校验码格式有误，2=时间戳校验失败，3=校验码校验失败
     * @throws Exception
     */
    public static int checkSign(String authKey, String serkey,
                                    String signMsg) throws Exception {
        return checkSign(authKey, serkey, defaultSeparator, signMsg);
    }

    public static void main(String[] args) {
        String authKey = "123456678sdfsafsd23423";  //准入授权码
        String serkey = String.valueOf(10001); //加密密钥（为用户ID）
        try {
            //生成校验码
            String sign = SignUtil.sign(authKey, serkey);
            System.out.println(sign);

            //校验校验码
            System.out.println(SignUtil.checkSign(authKey, serkey, sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
