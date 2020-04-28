package com.spark.bitrade.h5game.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.h5game.H5Resp;
import okhttp3.*;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * H5GameUtils
 *
 * @author archx
 * @time 2019/4/24 17:15
 */
public abstract class H5GameUtils {

    private static OkHttpClient defaultClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS).build();

    public static void setDefaultClient(OkHttpClient defaultClient) {
        H5GameUtils.defaultClient = defaultClient;
    }


    public static H5Resp post(String url, Map<String, String> params) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        FormBody.Builder fb = new FormBody.Builder();
        params.forEach(fb::add);
        FormBody form = fb.build();

        Request.Builder rb = new Request.Builder();
        Request req = rb.url(url).post(form).build();

        try (Response response = client.newCall(req).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null && mediaType.toString().contains("application/json")) {
                        return JSON.parseObject(body.string(), H5Resp.class);
                    }
                    if (mediaType != null && mediaType.toString().contains("text/html")) {
                        return H5Resp.builder().status(0).success(true).msg("ok").build();
                    }
                }
            }

        } catch (IOException | RuntimeException e) {
            // throw new RuntimeException(e.getMessage());
            return H5Resp.builder().status(-1).msg(e.getMessage()).build();
        }

        return H5Resp.builder().status(-1).msg("h5平台无返回").build();
    }

    /**
     * 构建请求参数
     *
     * @param mobile 标识符
     * @param amount 数额
     * @param appId  应用ID
     * @param secret 秘钥
     * @return map
     */
    public static Map<String, String> buildTransferParams(String mobile, String amount, String appId, String secret) {
        String tm = System.currentTimeMillis() / 1000 + "";
        String sign = md5(mobile + amount + tm + secret);
        String pm = getParamString(mobile, amount, tm, sign, secret);

        return map(new String[]{"appid", "pm", "tm"}, new String[]{appId, pm, tm});
    }

    /**
     * 构建联合登录参数
     *
     * @param mobile  标识符
     * @param amount  数额
     * @param inviter 邀请码
     * @param appId   应用ID
     * @param secret  秘钥
     * @return map
     */
    public static Map<String, String> buildUnionParams(String mobile, String amount, String inviter, String appId, String secret) {
        String tm = System.currentTimeMillis() / 1000 + "";
        String sign = md5(mobile + tm + inviter + secret);

        JSONObject param = new JSONObject();
        param.put("sign", sign);
        param.put("mobile", mobile);
        param.put("timestamp", tm);

        if (StringUtils.hasText(amount)) {
            param.put("amt", amount);
        }

        if (StringUtils.hasText(inviter)) {
            param.put("inviter", inviter);
        }


        String pm = encryptTripleDesToString(param.toJSONString(), secret);

        return map(new String[]{"appid", "pm", "tm"}, new String[]{appId, pm, tm});

    }

    private static Map<String, String> getNormalParamsMap(String mobile, String appId, String secret, String tm, String sign) {
        JSONObject param = new JSONObject();
        param.put("sign", sign);
        param.put("mobile", mobile);
        param.put("timestamp", tm);

        String pm = encryptTripleDesToString(param.toJSONString(), secret);

        return map(new String[]{"appid", "pm", "tm"}, new String[]{appId, pm, tm});
    }

    /**
     * 构建正常请求参数
     *
     * @param mobile 标识符
     * @param appId  应用ID
     * @param secret 秘钥
     * @return map
     */
    public static Map<String, String> buildNormalParams(String mobile, String appId, String secret) {
        String tm = System.currentTimeMillis() / 1000 + "";

        String sign = md5(mobile + tm + secret);

        return getNormalParamsMap(mobile, appId, secret, tm, sign);
    }

    /**
     * 获取加密参数
     *
     * @param mobile 标识符
     * @param amount 数额
     * @param tm     时间戳
     * @param sign   签名
     * @param secret 秘钥
     * @return pm
     */
    private static String getParamString(String mobile, String amount, String tm, String sign, String secret) {
        JSONObject param = new JSONObject();
        param.put("sign", sign);
        param.put("mobile", mobile);
        param.put("amt", amount);
        param.put("timestamp", tm);

        return encryptTripleDesToString(param.toJSONString(), secret);
    }

    /**
     * 3des加密
     *
     * @param content 内容
     * @param key     秘钥
     * @return 加密内容
     */
    private static String encryptTripleDesToString(String content, String key) {
        String result = null;
        try {
            DESedeKeySpec dks = new DESedeKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey sk = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(1, sk);
            byte[] bytes = cipher.doFinal(content.getBytes(UTF_8));
            BASE64Encoder encoder = new BASE64Encoder();
            result = encoder.encode(bytes).replaceAll("\r", "").replaceAll("\n", "");
        } catch (Exception var9) {
            var9.printStackTrace();
        }
        return result;
    }

    private static Map<String, String> map(String[] keys, String[] values) {
        Map<String, String> map = new HashMap<>();

        if (keys.length == values.length) {
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }
        }

        return map;
    }

    private static String md5(String inStr) {
        return md5(inStr.getBytes(Charset.forName("utf-8")));
    }

    private static String md5(byte[] bytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(bytes);

            byte[] arr = md5.digest();
            StringBuilder buf = new StringBuilder();
            for (byte b : arr) {
                int val = ((int) b) & 0xff;
                if (val < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(val));
            }
            return buf.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";

    }
}
