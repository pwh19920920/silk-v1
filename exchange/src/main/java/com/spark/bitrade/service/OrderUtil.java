package com.spark.bitrade.service;

/**
 *  
 *
 * @author young
 * @time 2019.09.18 13:57
 */
public class OrderUtil {
    /**
     * 校验是否为机器人订单
     *
     * @param orderId
     */
    public static boolean isRobotOrder(String orderId) {
        //订单格式：S1168423154092716041_SLUUSDT
        if (orderId == null) {
            return false;
        }

        if (!orderId.startsWith("S")) {
            return false;
        }
        if (!orderId.contains("_")) {
            return false;
        }

        return true;
    }
}
