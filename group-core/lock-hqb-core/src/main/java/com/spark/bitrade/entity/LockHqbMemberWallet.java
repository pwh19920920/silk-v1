package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 活期宝账户(LockHqbMemberWallet)实体类
 *
 * @author dengdy
 * @since 2019-04-23 15:48:39
 */
@Data
@TableName("lock_hqb_member_wallet")
@ApiModel(description = "活期宝账户")
public class LockHqbMemberWallet implements Serializable {

    private static final long serialVersionUID = 607468830139853953L;

    /**
     * 账户ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "账户ID", name = "id")
    private Long id;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", name = "memberId")
    private Long memberId;

    /**
     * 应用或渠道ID
     */
    @ApiModelProperty(value = "应用或渠道ID", name = "appId")
    private String appId;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种", name = "coinSymbol")
    private String coinSymbol;

    /**
     * 待确认数量（冻结余额）
     */
    @ApiModelProperty(value = "待确认数量（冻结余额）", name = "planInAmount")
    private BigDecimal planInAmount;

    /**
     * 已确认数量（锁定余额）
     */
    @ApiModelProperty(value = "已确认数量（锁定余额）", name = "lockamount")
    private BigDecimal lockAmount;

    /**
     * 累计收益,每次收益后统计
     */
    @ApiModelProperty(value = "累计收益,每次收益后统计", name = "accumulateIncome")
    private BigDecimal accumulateIncome;

    /**
     * 累计转入数量，用于数据平衡的校验
     */
    @ApiModelProperty(value = "累计转入数量，用于数据平衡的校验", name = "accumulateInAmount")
    private BigDecimal accumulateInAmount;

    /**
     * 累计转出数量，用于数据平衡的校验
     */
    @ApiModelProperty(value = "累计转出数量，用于数据平衡的校验", name = "accumulateOutAmount")
    private BigDecimal accumulateOutAmount;

}