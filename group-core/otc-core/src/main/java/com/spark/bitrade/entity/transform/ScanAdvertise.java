package com.spark.bitrade.entity.transform;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年01月12日
 */
@Builder
@Data
public class ScanAdvertise {
    private String memberName;
    private String avatar;
    private long advertiseId;
    /**
     * 交易次数
     */
    private int transactions;
    /**
     * 目前价格
     */
    private BigDecimal price;
    private BigDecimal minLimit;
    private BigDecimal maxLimit;
    /**
     * 剩余币数
     */
    private BigDecimal remainAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String payMode;
    private long coinId;
    private String unit;
    private String coinName;
    private String coinNameCn;
    /**
     * 0:未实名用户，1：实名用户，2：认证商家
     */
    private int level;

    private AdvertiseType advertiseType;

    private Country country;//add by tansitao 时间： 2018/8/15 原因：增加国家


    //========begin======1.3需求增加交易限制信息=====================
    /**
     * 需要交易方已绑定手机号
     */
    private BooleanEnum needBindPhone = BooleanEnum.IS_FALSE;

    /**
     * 需要交易方已做实名认证
     */
    private BooleanEnum needRealname = BooleanEnum.IS_FALSE;

    /**
     * 需要交易方至少完成过N笔交易（默认为0）
     */
    private int needTradeTimes = 0;
    //=======end=======1.3需求增加交易限制信息=====================

}
