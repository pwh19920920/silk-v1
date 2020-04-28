package com.spark.bitrade.entity;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CoinFeeType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年01月29日
 */
@Builder
@Data
public class WithdrawWalletInfo {
    private String unit;
    /**
     * 阈值
     */
    private BigDecimal threshold;
    /**
     * 最小提币数量
     */
    private BigDecimal minAmount;
    /**
     * 最大提币数量
     */
    private BigDecimal maxAmount;
    private double minTxFee;
    private double maxTxFee;
    private String nameCn;
    private String name;
    private BigDecimal balance;
    private BooleanEnum canAutoWithdraw;
    private int withdrawScale; //add by yangch 时间： 2018.04.24 原因：合并新增
    /**
     * 手续费率设置
     */

    private BigDecimal feeRate;

    /**
     * 手续费模式1，固定，2比例
     */
    @Enumerated(EnumType.ORDINAL)
    private CoinFeeType feeType;

    /**
     * 地址
     */
    private List<Map<String,String>> addresses;

    //add by  shenzucai 时间： 2018.10.29  原因： 匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 start
    /**
     * 手续费抵扣币种单位（不包括当前币种）
     */
    private String feeDiscountCoinUnit;

    /**
     * 手续费抵扣模式
     */
    @Enumerated(EnumType.ORDINAL)
    private CoinFeeType feeDiscountType;

    /**
     * 抵扣数值，手续费抵扣模式为0时，代表具体数量，抵扣模式为1时，代表折扣0.8代表8折
     */
    private BigDecimal feeDiscountAmount;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end

    /**
     * 是否具有标签，0有1无
     */
    private Integer hasLabel;


}
