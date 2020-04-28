package com.spark.bitrade.constant;

import java.math.BigDecimal;

/***
 * 锁仓模块的常量
 * @author yangch
 * @time 2018.12.03 18:03
 */
public class LockConstant {
    //kafka topic 常量
    public final static String KAFKA_TX_CNYT_MESSAGE_HANDLER = "tx_cnyt_message_handler";


    //默认活动币种，用于兼容cnyt活动
    public final static String DEFAULT_CNYT_SYMBOL = "CNYT";

    /**
     * 虚拟的市场等级
     */
    public final static int VIRTUAL_MARKET_LEVEL = 0;

    //增加周期天数
    public final static int PERIOD = 30;

    //增加周期天数
    public final static BigDecimal TRAINING_RATE = BigDecimal.valueOf(0.1);


}
