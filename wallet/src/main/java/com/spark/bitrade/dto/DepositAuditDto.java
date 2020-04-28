package com.spark.bitrade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author shenzucai
 * @time 2019.09.04 10:59
 */
@Data
public class DepositAuditDto {

    /**
     * 用户id
     */
    private Long memberId;
    /**
     * 地址
     */
    private String address;

    /**
     * 交易id
     */
    private String txid;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种单位
     */
    private String unit;

    /**
     * 时间
     */
    private Date time;

    /**
     * 审核结果，通过 true，失败 false
     */
    private Boolean result;
}
