package com.spark.bitrade.utils;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.util.MD5Util;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * <p>签名计算及解析</p>
 * @author octopus
 * @date 2018-10-8
 */
public class SignUtil {

    /**
     * 签名算法
     * @param timestamp
     *            时间戳
     * @param jsonObject
     *            签名内容
     * @return
     */
    public static String sign(long timestamp, JSONObject jsonObject){
        if (null == jsonObject){
            return null;
        }
        String sign = "";
        //迭代器
        Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
        List<String> keyList = new ArrayList<>();
        //提取key
        for (Map.Entry<String, Object> entry : entrySet){
            String key = entry.getKey();
            keyList.add(key);
        }
        //key字典排序
        String[] keys = new String[keyList.size()];
        keys = keyList.toArray(keys);
        Arrays.sort(keys);

        String[] splitJoin = new String[keys.length + 1];
        StringBuilder stringBuilder = null;
        //拼接参数
        for (int i=0; i<keys.length; i++){
            stringBuilder = new StringBuilder();
            String key = keys[i];
            String keyValue = stringBuilder.append(key).append("=").append(jsonObject.get(key)).toString();
            splitJoin[i] = keyValue;
        }
        stringBuilder = new StringBuilder();
        splitJoin[keys.length] = stringBuilder.append("timestamp").append("=").append(timestamp).toString();

        //用‘&’符号链接参数
        String stringJoin = StringUtils.join(splitJoin,"&");
        sign = MD5Util.md5Encode(stringJoin).toLowerCase();
        return sign;
    }

}
