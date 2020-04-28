package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.ExchangeCoinDisplayArea;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeCoin implements Serializable {
    private static final long serialVersionUID = 7898194272883238670L;

    //交易对名称，格式：BTC/USDT
    @NotNull(message = "交易对不能为空")
    @Id
    private String symbol;
    //交易币种符号
    private String coinSymbol;
    //结算币种符号，如USDT
    private String baseSymbol;

    //状态，1：启用，2：禁止
    private int enable;
    //交易手续费
    @Column(columnDefinition = "decimal(8,4) comment '交易手续费'")
    private BigDecimal fee;
    //排序，从小到大
    private int sort;
    //交易币小数精度
    private int coinScale;
    //基币小数精度
    private int baseCoinScale;
    @Column(columnDefinition = "decimal(18,8) default 0 comment '最低挂单卖价'")
    private BigDecimal minSellPrice;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 1 comment '是否启用市价卖'")
    private BooleanEnum enableMarketSell = BooleanEnum.IS_TRUE;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 1 comment '是否启用市价买'")
    private BooleanEnum enableMarketBuy = BooleanEnum.IS_TRUE;

    /**
     * 最大交易时间
     */
    @Column(columnDefinition = "int(11) default 0 comment '委托超时自动下架时间，单位为秒，0表示不过期'")
    private int maxTradingTime;

    /**
     * 最大在交易中的委托数量
     */
    @Column(columnDefinition = "int(11) default 0 comment '最大允许同时交易的订单数，0表示不限制'")
    private int maxTradingOrder;

    /**
     * 标签位，用于推荐，排序等,默认为0，1表示推荐，
     */
    @Column(columnDefinition = "int(11) default 0 ")
    private int flag;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '最小挂单成交额'")
    private BigDecimal minTurnover;

    /**
     * 最小挂单数量 -> 最小买单挂单数量
     */
    @Column(columnDefinition = "decimal(24,8) default 0 comment '最小买单挂单数量'")
    private BigDecimal minAmount;

    /**
     * 最小卖单挂单数量
     */
    @Column(columnDefinition = "decimal(24,8) default 0 comment '最小卖单挂单数量'")
    private BigDecimal minSellAmount;

    //add by yangch 时间： 2018.06.01 原因：折扣=discount
    // 吃单：买币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment ' 吃单：买币手续费的折扣率'")
    private BigDecimal feeBuyDiscount;

    // 吃单：卖币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment ' 吃单：卖币手续费的折扣率'")
    private BigDecimal feeSellDiscount;

    // 挂单：买币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment ' 挂单：买币手续费的折扣率'")
    private BigDecimal feeEntrustBuyDiscount;

    // 挂单：卖币手续费的折扣率，用小数表示百分比（表示减免的手续费）。eg：20%=0.2；1为手续费全免，0为不优惠手续费
    @Column(columnDefinition = "decimal(8,4) default 0 comment ' 挂单：卖币手续费的折扣率'")
    private BigDecimal feeEntrustSellDiscount;

    //币种区域，主币区域，创新版区域
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 0 comment '币种显示区域'")
    private ExchangeCoinDisplayArea displayArea;


    //默认是否显示 isShow 1 显示  0 不显示
    @Column(columnDefinition = "int(2) default 1 comment '默认是否显示'")
    private Integer isShow = 1;

    /**
     * 交易验证码
     */
    private String tradeCaptcha;

    /**
     * 排名：最低交易笔数
     */
    private Integer rankMinTradeTimes;

    /**
     * 排名：最低交易成数量
     */
    private BigDecimal rankMinTradeAmount;

}
