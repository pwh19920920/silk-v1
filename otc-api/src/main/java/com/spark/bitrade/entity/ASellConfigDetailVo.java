package com.spark.bitrade.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ASellConfigDetailVo {
  /** 用户id */
  private Long memberId;
  /** 佣金奖励 */
  @ApiModelProperty(value = "奖励佣金率")
  private BigDecimal awardRate;
  /** 推荐奖佣金 */
  @ApiModelProperty(value = "推荐奖励率")
  private BigDecimal referrerRate;
  /** 手续费率 */
  @ApiModelProperty(value = "手续费率")
  private BigDecimal fee;
  /** 收款渠道 */
  @ApiModelProperty(value = "收款渠道")
  private String payMode;
  /** 是否接单 1 接单 0不接单 */
  @ApiModelProperty(value = "商家当前是否可接单：1 接单")
  private Boolean orderEnable;
  /** 最小额 */
  @ApiModelProperty(value = "商家设置交易最低金额，不能低于系统配置")
  private BigDecimal transferMinAmount;
  /** 最大额 */
  @ApiModelProperty(value = "商家设置交易最高金额，不能高于系统配置")
  private BigDecimal transferMaxAmount;
  /** 商家设置同时进行的最大订单数。 0 不限制 */
  @ApiModelProperty(value = "商家设置同时进行的最大订单数。 0 不限制")
  private Integer maxTradingOrder;
  /** 可用余额 */
  @ApiModelProperty(value = "可用余额")
  private BigDecimal balance;
}
