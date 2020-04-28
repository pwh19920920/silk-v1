package com.spark.bitrade.entity;

import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

/**
 * @author Zhang Jinwei
 * @date 2018年01月16日
 */
@Builder
@Data
public class PreOrderInfo {
    private String username;
    private BooleanEnum emailVerified;
    private BooleanEnum phoneVerified;
    private BooleanEnum idCardVerified;
    private int transactions; //广告主的交易次数
    private long otcCoinId;
    private String unit;
    private BigDecimal price;
    private BigDecimal number;
    private String payMode;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    private int timeLimit;
    private String country;
    /**
     * 法币ID add by ss 时间：2020/03/26 原因：添加法币
     */
    private Long currencyId;

    /**
     * 法币名称
     */
    private String currencyName;

    /**
     * 法币单位
     */
    private String currencyUnit;

    /**
     * 法币符号
     */
    private String currencySymbol;
    private AdvertiseType advertiseType;
    private String remark;
    private int selfTransactions;//用户自己的交易次数
    private BigDecimal serviceRate; // 服务费率

    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum memberLevel;//广告主的身份
    /**
     * 需要交易方已绑定手机号
     */
    private BooleanEnum needBindPhone;

    /**
     * 需要交易方已做实名认证
     */
    private BooleanEnum needRealname;

    /**
     * 需要交易方至少完成过N笔交易（默认为0）
     */
    private int needTradeTimes;

    /**
     * 货币小数位精度（默认为8位）
     */
    private int coinScale;

    //add by tansitao 时间： 2018/11/20 原因：交易订单数
    private int tradingOrderNume = 0;

    //add by tansitao 时间： 2018/11/19 原因：同时最大处理订单数 (0 = 不限制)
    private int maxTradingOrders = 0;
}
