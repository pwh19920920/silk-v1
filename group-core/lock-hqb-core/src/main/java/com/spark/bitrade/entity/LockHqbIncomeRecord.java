package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 活期宝收益记录
 *
 * @author Zhang Yanjun
 * @time 2019.04.23 11:48
 */
@Data
@TableName("lock_hqb_income_record")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "活期宝收益记录")
public class LockHqbIncomeRecord {

    /**
     * id
     */
    @TableId(type = IdType.NONE)
    @ApiModelProperty(value = "id", name = "id")
    private Long id;

    /**
     * 活期宝账户ID
     */
    @ApiModelProperty(value = "活期宝账户ID", name = "walletId")
    private Long walletId;

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
     * 收益数量
     */
    @ApiModelProperty(value = "收益数量", name = "incomeAmount")
    private BigDecimal incomeAmount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private Long createTime;

    /**
     * 更新日期
     */
    @ApiModelProperty(value = "更新日期", name = "updateDate")
    private Long updateDate;

}














