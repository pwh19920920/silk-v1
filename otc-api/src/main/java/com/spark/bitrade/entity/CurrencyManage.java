package com.spark.bitrade.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 法币管理
 * </p>
 *
 * @author qiliao
 * @since 2020-03-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class CurrencyManage implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    @ApiModelProperty(value = "ID")
    private Long id;

    /**
     * 法币名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 法币单位
     */
    @ApiModelProperty(value = "单位")
    private String unit;

    /**
     * 法币符号
     */
    @ApiModelProperty(value = "符号")
    private String symbol;

    /**
     * 最低单笔交易额
     */
    @ApiModelProperty(value = "最低单笔交易额")
    private BigDecimal minAmount;

    /**
     * 最高单笔交易额
     */
    @ApiModelProperty(value = "最高单笔交易额")
    private BigDecimal maxAmount;

    /**
     * 交易方式限制（0 all 1 or）
     */
    @ApiModelProperty(value = "交易方式限制 0 绑定所有支付方式  1 绑定其中一种支付方式")
    private String payTypeSetting;

    /**
     * 交易方式（逗号分隔）
     */
    @ApiModelProperty(value = "收付款方式，逗号分隔")
    private String paySetting;

    /**
     * 是否启用
     */
    @ApiModelProperty(value = "是否启用 1 启用  0未启用")
    private String currencyState;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private String currencyOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Long updateId;


}
