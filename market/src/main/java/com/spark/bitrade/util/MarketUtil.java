package com.spark.bitrade.util;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeTrade;

/**
 *  
 *
 * @author young
 * @time 2019.09.09 16:00
 */
public class MarketUtil {
    private static String ROBOT_ORDER_PREFIX = "S";
    private static String ROBOT_ORDER_SEPARATOR = "_";

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

        if (!orderId.startsWith(ROBOT_ORDER_PREFIX)) {
            return false;
        }
        if (!orderId.contains(ROBOT_ORDER_SEPARATOR)) {
            return false;
        }

        return true;
    }

    /**
     * 是否为机器人订单
     *
     * @param trade
     * @param direction
     * @return
     */
    public static boolean isRobotOrder(ExchangeTrade trade, ExchangeOrderDirection direction) {
        if (direction == ExchangeOrderDirection.BUY) {
            if (isRobotOrder(trade.getBuyOrderId())) {
                return true;
            }
        } else {
            if (isRobotOrder(trade.getSellOrderId())) {
                return true;
            }
        }

        return false;
    }
}
