package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
  * 业务模块
  * @author yangch
  * @time 2018.06.05 13:54
  */
@AllArgsConstructor
@Getter
public enum BusinessErrorMonitorType implements BaseEnum {
    /**
     * 0
     */
    UNKNOWN("未知错误"),
    /**
     * 1  接口：exchangeOrderService.processExchangeTrade(trade)
     */
    @Deprecated
    EXCHANGE__PROCESS_TRADE("撮单成功后成交明细处理失败"),
    /**
     * 2  接口：exchangeOrderService.tradeCompleted()
     * market端分发：已完成的任务（集合）失败
     */
    EXCHANGE__SEND_COMPLETED("分发已完成的任务失败"),
    /**
     * 3  接口：kafkaTemplate.send("exchange-order",symbol, JSON.toJSONString(order));
     */
    @Deprecated
    EXCHANGE__KAFKA_SEND_ORDER("订单消息发送失败"),
    /**
     * 4  接口：exchangeOrderService.processExchangeTrade(trade,direction)
     * market端分发：买单成交明细的任务（集合）失败
     */
    EXCHANGE__SEND_TRADE_BUY("分发买单成交明细的任务失败"),
    /**
     * 5  接口：exchangeOrderService.processExchangeTrade(trade,direction)
     * market端分发：卖单成交明细的任务（集合）失败
     */
    EXCHANGE__SEND_TRADE_SELL("分发卖单成交明细的任务失败"),
    /**
     * 6  接口：exchangeOrderService.returnOrderBalance(order)
     */
    @Deprecated
    EXCHANGE__ORDER_RETURN_BALANCE_FAIL("归还订单余额失败"),

    // 机器人api：7 - 11
    /**
     * 7  接口：exchangeOrderService.order, isCanceledSuccessed)
     */
    EXCHANGE__CYW_CANCEL_FAIL("机器人订单撤销失败"),

    /**
     * 8  机器人api：已完成的机器人订单入库失败
     */
    EXCHANGE__CYW_COMPLETED("已完成的机器人订单入库失败"),
    /**
     * 9  机器人api：机器人买单成交明细处理失败
     */
    EXCHANGE__CYW_TRADE_BUY("机器人买单成交明细处理失败"),
    /**
     * 10  机器人api：机器人卖单成交明细处理失败
     */
    EXCHANGE__CYW_TRADE_SELL("机器人卖单成交明细处理失败"),
    /**
     * 11  机器人api：机器人订单校验失败
     */
    EXCHANGE__CYW_CHECK_FAIL("机器人订单校验失败"),


    // 机器人api：12 - 16
    /**
     * 12  币币：用户订单撤销失败
     */
    EXCHANGE__USER_CANCEL_FAIL("用户订单撤销失败"),

    /**
     * 13  币币：已完成订单处理失败
     */
    EXCHANGE__USER_COMPLETED("已完成订单处理失败"),
    /**
     * 14  币币：买单成交明细处理失败
     */
    EXCHANGE__USER_TRADE_BUY("买单成交明细处理失败"),
    /**
     * 15  币币：卖单成交明细处理失败
     */
    EXCHANGE__USER_TRADE_SELL("卖单成交明细处理失败"),
    /**
     * 16  币币：订单校验失败
     */
    EXCHANGE__USER_CHECK_FAIL("订单校验失败"),
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
