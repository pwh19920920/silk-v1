package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * silubium-silktrader交易记录表
 * </p>
 *
 * @author shenzucai
 * @since 2019-01-21
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferSilubium implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 星客用户id
     */
    private Long userId;
    /**
     * 星客钱包（member_wallet_id）
     */
    private Long walletId;
    /**
     * 转入地址
     */
    private String toAddress;
    /**
     * 转出地址
     */
    private String fromAddress;
    /**
     * 转入地址标签
     */
    private String toLabel;
    /**
     * 转出地址标签
     */
    private String fromLabel;
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    /**
     * 到账金额
     */
    private BigDecimal arriveAmount;
    /**
     * 币种（币种单位大小，参见Coin表unit）
     */
    private String coinUnit;
    /**
     * 手续费
     */
    private BigDecimal fee;
    /**
     * 主币手续费
     */
    private BigDecimal baseCoinFree;
    /**
     * 抵扣币种对应手续费
     */
    private BigDecimal feeDiscountAmount;
    /**
     * 手续费抵扣币种单位（不包括当前币种）
     */
    private String feeDiscountCoinUnit;
    /**
     * 0 转入，1 转出
     */
    private Integer transferType;

    /**
     * 0转入 时为交易hash值，1 转出 时为提币记录id
     */
    private String transferHash;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 0 发起转账，1 转账成功，2 转账失败
     */
    private Integer transferStatus;
    /**
     * 备注，扩展字段
     */
    private String comment;

    /**
     * 应用id
     */
    private String appId;

}
