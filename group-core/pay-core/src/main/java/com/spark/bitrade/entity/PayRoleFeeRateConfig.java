package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.CoinFeeType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 收支角色手续费率配置表
 * （ 按角色设置收支手续费率）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@Data
@TableName("pay_role_fee_rate_config")
public class PayRoleFeeRateConfig {
    ///@TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名
     */
    private String roleName;
    /**
     * 支付手续费
     */
    private BigDecimal payFee = BigDecimal.ZERO;
    /**
     * 交易币种
     */
    private String tradeUnit;
    /**
     * 手续费币种
     */
    private String feeUnit;
    /**
     * 手续费类型（固定值、百分比）
     */
    private CoinFeeType feeType;
    /**
     * 收款手续费
     */
    private BigDecimal incomeFee;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
