package com.spark.bitrade.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具类
 * @author shenzucai
 * @time 2018.07.01 15:00
 */
@Component(value = "Base64Util")
public class Base64Util {


//    private static final String SKEY = PropertiesUtils.PAY.getProperty("pay.encode.skey"); // 固定密钥

    private static String SKEY;

    public static String getSKEY(){
        return SKEY;
    }

    @Value("${pay.encode.skey}")
    public void setSKEY(String SKEY){
        Base64Util.SKEY = SKEY;
    }

    private static String IVPARAMETER;

    public static String getIVPARAMETER(){
        return IVPARAMETER;
    }

    @Value("${pay.encode.ivparameter}")
    public void setIVPARAMETER(String IVPARAMETER){
        Base64Util.IVPARAMETER = IVPARAMETER;
    }

//    private static final String IVPARAMETER = PropertiesUtils.PAY.getProperty("pay.encode.ivparameter"); // 密钥偏移量IVPARAMETER
    private static final String ENCODEINGFORMAT = "utf-8";


    /**
     * base64加密
     *
     * @param string
     * @return
     */
    public static String encodeAES(final String string) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 补码方式
            byte[] raw = SKEY.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES"); // 使用算法名字
            IvParameterSpec iv = new IvParameterSpec(IVPARAMETER.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv); // 初始化模式为加密模式，并指定密匙
            byte[] encrypted = cipher.doFinal(string.getBytes(ENCODEINGFORMAT));
            String encryptedStr = new BASE64Encoder().encode(encrypted);
            return encryptedStr.toString().replaceAll("\r\n", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * base64解密
     *
     * @param string
     * @return
     */
    public static String decodeAES(final String string) {
        try {
            byte[] raw = SKEY.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES"); // 使用算法名
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 使用CBC模式，PKCS5Padding补全方式
            IvParameterSpec iv = new IvParameterSpec(IVPARAMETER.getBytes());// 偏移量
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv); // 初始化模式为解密模式，并指定密匙
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(string);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, ENCODEINGFORMAT);
            return originalString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeDES(final String string,String DKEY) {
        try {
            DESKeySpec keySpec = new DESKeySpec(DKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IVPARAMETER.substring(0,8).getBytes()));
            byte[] result = cipher.doFinal(string.getBytes(ENCODEINGFORMAT));
            String encryptedStr = new BASE64Encoder().encode(result);
            return encryptedStr.toString().replaceAll("\r\n", "").trim();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("exception:" + e.toString());
        }
        return null;
    }

    public static String decodeDES(final String string,String DKEY) {
        try {
            DESKeySpec keySpec = new DESKeySpec(DKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IVPARAMETER.substring(0,8).getBytes()));
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(string);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, ENCODEINGFORMAT);
            return originalString;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("exception:" + e.toString());
        }
        return null;
    }
}
