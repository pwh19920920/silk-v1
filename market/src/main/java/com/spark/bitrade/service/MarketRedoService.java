package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.BusinessErrorMonitor;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.util.MarketUtil;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 *  重做服务
 *
 * @author young
 * @time 2019.09.10 09:40
 */
@Service
public class MarketRedoService {
    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;
    @Autowired
    private ICywService cywService;
    @Autowired
    private IExchange2Service exchange2Service;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public MessageResult redo(Long id) {
        BusinessErrorMonitor businessErrorMonitor = businessErrorMonitorService.findOne(id);
        if (null == businessErrorMonitor) {
            return MessageResult.error("指定的业务重做记录不存在");
        }

        //已处理则返回
        if (businessErrorMonitor.getMaintenanceStatus() == BooleanEnum.IS_TRUE) {
            return MessageResult.success();
        }

        try {
            MessageResult result;
            if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_BUY
                    || businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_SELL) {
                // 分发 成交明细任务
                ExchangeTrade trade = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeTrade.class);
                if (null == trade) {
                    return MessageResult.error("撮单成交明细参数为空");
                }
                result = this.redoSendTradeTask(businessErrorMonitor, trade);
            } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__SEND_COMPLETED) {
                // 分发 已完成的任务
                ExchangeOrder order = JSON.parseObject(businessErrorMonitor.getInData(), ExchangeOrder.class);
                if (null == order) {
                    return MessageResult.error("订单参数为空");
                }

                result = this.redoSendCompleteTask(order);
            } else if (businessErrorMonitor.getType().name().startsWith("EXCHANGE__CYW")) {
                // 兼容：调用机器人服务的重做接口
                return cywService.redo(id);
            } else if (businessErrorMonitor.getType().name().startsWith("EXCHANGE__USER")) {
                // 兼容：调用用户币币交易的重做接口
                return exchange2Service.redo(id);
            } else {
                return MessageResult.error("该接口不支持该业务重做");
            }

            if (result.getCode() == 0) {
                //更改重做记录状态
                businessErrorMonitor.setMaintenanceStatus(BooleanEnum.IS_TRUE);
                businessErrorMonitor.setMaintenanceResult("业务重做成功");
                businessErrorMonitorService.save(businessErrorMonitor);

                return result;
            } else {
                throw new Exception(result.getMessage());
            }
        } catch (Exception e) {
            try {
                //保存重做记录错误信息
                businessErrorMonitor.setMaintenanceResult(e.getMessage());
                businessErrorMonitorService.update4NoRollback(businessErrorMonitor);
            } catch (Exception ex) {
            }
            return MessageResult.error("业务重做出错，错误信息：" + e.getMessage());
        }
    }

    /**
     * 重新分发成交明细任务
     *
     * @param businessErrorMonitor
     * @param trade
     * @return
     * @throws Exception
     */
    private MessageResult redoSendTradeTask(BusinessErrorMonitor businessErrorMonitor, ExchangeTrade trade) throws Exception {
        if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_BUY) {
            if (MarketUtil.isRobotOrder(trade, ExchangeOrderDirection.BUY)) {
                kafkaTemplate.send("exchange-cyw-trade", ExchangeOrderDirection.BUY.name(), JSON.toJSONString(trade));
            } else {
                kafkaTemplate.send("exchange-user-trade", ExchangeOrderDirection.BUY.name(), JSON.toJSONString(trade));
            }
        } else if (businessErrorMonitor.getType() == BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_SELL) {
            if (MarketUtil.isRobotOrder(trade, ExchangeOrderDirection.SELL)) {
                kafkaTemplate.send("exchange-cyw-trade", ExchangeOrderDirection.SELL.name(), JSON.toJSONString(trade));
            } else {
                kafkaTemplate.send("exchange-user-trade", ExchangeOrderDirection.SELL.name(), JSON.toJSONString(trade));
            }
        } else {
            return MessageResult.error("重做类型错误");
        }

        return MessageResult.success();
    }

    /**
     * 重新分发已完成的订单任务
     *
     * @param order
     * @return
     * @throws UnexpectedException
     */
    private MessageResult redoSendCompleteTask(ExchangeOrder order) {
        if (MarketUtil.isRobotOrder(order.getOrderId())) {
            kafkaTemplate.send("exchange-cyw-order-completed", order.getSymbol(), JSON.toJSONString(order));
        } else {
            kafkaTemplate.send("exchange-user-order-completed", order.getSymbol(), JSON.toJSONString(order));
        }
        return MessageResult.success();
    }
}
