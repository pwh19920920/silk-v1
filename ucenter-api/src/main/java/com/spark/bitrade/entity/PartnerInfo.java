package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PromotionLevel;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年03月20日
 */
@Data
public class PartnerInfo {
    private String area;
    /**
     * 等级
     */
    @Enumerated(EnumType.ORDINAL)
    private PartnerLevle level;

    /**
     * 本月累计新增用户数
     */
    private long monthAddUserNum;

    /**
     * 本月累计交易量（USDT）
     */
    private BigDecimal monthTradeAmount = BigDecimal.ZERO;

    /**
     * 当月累计收益（USDT）
     */

    private BigDecimal monthIncomeAmount = BigDecimal.ZERO;


    /**
     * 总累计新增用户数
     */
    private long allAddUserNum;

    /**
     * 总累计交易量（USDT）
     */
    private BigDecimal allTradeAmount = BigDecimal.ZERO;

    /**
     * 总累计收益（USDT）
     */
    private BigDecimal allIncomeAmount = BigDecimal.ZERO;
}
