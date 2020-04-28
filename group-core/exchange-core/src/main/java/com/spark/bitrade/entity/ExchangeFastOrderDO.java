package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 闪兑订单
 *
 * @author Zhang Yanjun
 * @time 2019.03.27 11:10
 */
@Data
@TableName("exchange_fast_order")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "闪兑订单")
public class ExchangeFastOrderDO {
    /**
     * 订单ID
     */
    @TableId
    @ApiModelProperty(value = "订单ID", example = "")
    private Long orderId;

    /**
     * 总账户ID
     */
    @ApiModelProperty(value = "总账户ID", example = "", required = true)
    private Long memberId;

    /**
     * 闪兑基币币种名称,如CNYT、BT
     */
    @ApiModelProperty(value = "闪兑基币币种名称,如CNYT、BT", example = "CNYT", required = true)
    private String baseSymbol;

    /**
     * 闪兑币种名称，如BTC、LTC
     */
    @ApiModelProperty(value = "闪兑币种名称，如BTC、LTC", example = "BTC", required = true)
    private String coinSymbol;

    /**
     * 闪兑数量，由闪兑用户输入的数量
     */
    @ApiModelProperty(value = "闪兑数量", example = "100.00", required = true)
    private BigDecimal amount;

    /**
     * 成交数量，根据闪兑规则计算得到的成交数量
     */
    @ApiModelProperty(value = "成交数量，根据闪兑规则计算得到的成交数量", example = "100.00")
    private BigDecimal tradedAmount;

    /**
     * 订单方向:买入/卖出
     */
    @ApiModelProperty(value = "订单方向:0=买入/1=卖出", example = "0")
    private ExchangeOrderDirection direction;

    /**
     * 闪兑调整比例，取值[0-1]，冗余数据，记录成交时基于实时汇率价格的调整比例
     */
    @ApiModelProperty(value = "闪兑调整比例，取值[0-1]", example = "0.05")
    private BigDecimal adjustRate;

    /**
     * 实时汇率价（即实时价格），冗余数据，记录成交当时的实时汇率
     */
    @ApiModelProperty(value = "实时汇率价", example = "1.05")
    private BigDecimal currentPrice;

    /**
     * 成交价，根据实时汇率、闪兑浮动比例以及方向计算出来的成交价
     */
    @ApiModelProperty(value = "成交价", example = "1.01")
    private BigDecimal tradedPrice;

    /**
     * 兑换发起方处理状态：0=TRADING（交易中）/1=COMPLETED(完成)
     */
    @ApiModelProperty(value = "兑换发起方处理状态,0=TRADING（交易中）/1=COMPLETED(完成)", example = "1")
    private ExchangeOrderStatus initiatorStatus;

    /**
     * 兑换接收方处理状态：0=TRADING（交易中）/1=COMPLETED(完成)
     */
    @ApiModelProperty(value = "兑换接收方处理状态,0=TRADING（交易中）/1=COMPLETED(完成)", example = "0")
    private ExchangeOrderStatus receiverStatus;

    /**
     * 下单时间
     */
    @ApiModelProperty(value = "下单时间，时间戳", example = "1540194175306")
    private Long createTime;

    /**
     * 成交时间
     */
    @ApiModelProperty(value = "成交时间，时间戳", example = "1540194175306")
    private Long completedTime;

    /**
     * 虚拟佣金，冗余数据；兑总账户获得的虚拟收益=计算并记录基于实时汇率和调整后的汇率计算出来的虚拟收益。
     */
    @ApiModelProperty(value = "虚拟佣金", example = "0.05")
    private BigDecimal virtualBrokerageFee;

    /**
     * 兑换接收方用户ID
     */
    @ApiModelProperty(value = "兑换接收方用户ID", example = "", required = true)
    private Long receiveId;

    /**
     * 渠道/应用ID
     */
    @ApiModelProperty(value = "应用ID", example = "0", required = true)
    private String appId;

}
