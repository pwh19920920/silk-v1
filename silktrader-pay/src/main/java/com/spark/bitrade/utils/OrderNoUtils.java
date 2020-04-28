package com.spark.bitrade.utils;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author shenzucai
 * @time 2018.07.02 15:33
 */
public class OrderNoUtils {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    static{
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置北京时区
    }
    /**
     * 生成三方支付订单号
     * @author shenzucai
     * @time 2018.07.02 15:34
     * @param
     * @return true
     */
    public static String getOrderNo(String busiId,String userId){
        // 平台订单号yyyyMMddHHmmssSSS-平台商户id-平台顾客id 理论上一个用户在一个商家下1秒钟下单一千次的可能性接近0
        Date date = new Date();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(simpleDateFormat.format(date));
        stringBuilder.append("-"+busiId);
        stringBuilder.append("-"+userId);
        return stringBuilder.toString();
    }
}
