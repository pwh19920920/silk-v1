package com.spark.bitrade.constant;

/**
 * <p>红包常量类</p>
 * @author octopus
 */
public class RedPacketConstant {

    //小数保留位数
    public static final int DIGITS = 8;

    //红包redis缓存常量
    public static String JACKPOT_BALANCE = "entity:bettingConfig:jackpot_balance";

    public static String JACKPOT_BALANCE_FINISHED = "entity:bettingConfig:jackpot_balance_finished";
}
