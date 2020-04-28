package com.spark.bitrade.utils;

import org.springframework.beans.factory.annotation.Value;
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
public class DesBase64Util {



    private static final String ENCODEINGFORMAT = "utf-8";

    public static String encodeDES(final String string,String SKEY) {
        try {
            DESKeySpec keySpec = new DESKeySpec(SKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(string.getBytes(ENCODEINGFORMAT));
            String encryptedStr = new BASE64Encoder().encode(result);
            return encryptedStr.toString().replaceAll("\r\n", "").trim();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("exception:" + e.toString());
        }
        return null;
    }

    public static String decodeDES(final String string,String SKEY) {
        try {
            DESKeySpec keySpec = new DESKeySpec(SKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
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
