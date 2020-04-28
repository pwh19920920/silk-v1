package com.spark.shiro.util;

import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;

/**
 * 密码加密工具类
 */
public class PasswordUtil {
    //默认加密算法
    public static final String DEFAULT_ALGORITHM="SHA-512";
    /**
     * 加密具体方法，使用shiro提供的加密方法
     * @param password
     * @param salt
     * @return
     */
    public static String digestEncodedPassword(final String password,String salt){
        final ConfigurableHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName(DEFAULT_ALGORITHM);
        hashService.setHashIterations(0);
        final HashRequest request = new HashRequest.Builder()
                .setSalt(salt)
                .setSource(password)
                .build();
        return hashService.computeHash(request).toHex();
    }
}
