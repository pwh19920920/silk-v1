package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.util.MarketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 成交订单明细处理
 *
 * @author yangch
 * @time 2018.07.02 10:01  
 */

@Service
@Slf4j
public class ProcessExchangeTradeService {
    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;

    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 异步分发成交明细任务（按买单和卖单拆分处理）
     *
     * @param trade       匹配订单
     * @param direction   订单类型
     * @param kafkaOffset kafka offset参数
     * @return
     * @throws Exception
     */
    @Async("trade")
    public void asyncSendExchangeTradeTask(ExchangeTrade trade, ExchangeOrderDirection direction, long kafkaOffset) {
        if (direction == ExchangeOrderDirection.BUY) {
            this.sendTradeBuy(trade, direction);
        } else {
            this.sendTradeSell(trade, direction);
        }

        //Redis系统中移除待消费的消息标识
        if (kafkaOffset >= 0) {
            kafkaOffsetCacheService.removeListItem(
                    KafkaOffsetCacheKeyConstant.
                            getKeyExchangeTrade(trade.getSymbol(), direction), 1, kafkaOffset);
        }
    }

    /**
     * 发送 买方成交信息
     *
     * @param trade
     */
    private void sendTradeBuy(ExchangeTrade trade, ExchangeOrderDirection direction) {
        try {
            if (isRobotOrder(trade, direction)) {
                //log.info("机器人买方成交信息：{}", trade);
                kafkaTemplate.send("exchange-cyw-trade", ExchangeOrderDirection.BUY.name(), JSON.toJSONString(trade));
            } else {
                //log.info("用户买方成交信息：{}", trade);
                kafkaTemplate.send("exchange-user-trade", ExchangeOrderDirection.BUY.name(), JSON.toJSONString(trade));
            }
        } catch (Exception e) {
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_BUY, trade.toString(), e.getMessage());
            } catch (Exception ex) {
                log.error("订单明细处理失败，需手工处理，订单类型:{}，撮单信息:{}", ExchangeOrderDirection.BUY.toString(), trade);
                log.error("订单明细处理失败，需手工处理", e);
            }
        }
    }

    /**
     * 发送 卖方成交信息
     *
     * @param trade
     */
    private void sendTradeSell(ExchangeTrade trade, ExchangeOrderDirection direction) {
        try {
            if (isRobotOrder(trade, direction)) {
                //log.info("机器人卖方成交信息：{}", trade);
                kafkaTemplate.send("exchange-cyw-trade", ExchangeOrderDirection.SELL.name(), JSON.toJSONString(trade));
            } else {
                //log.info("用户卖方成交信息：{}", trade);
                kafkaTemplate.send("exchange-user-trade", ExchangeOrderDirection.SELL.name(), JSON.toJSONString(trade));
            }
        } catch (Exception e) {
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__SEND_TRADE_SELL, trade.toString(), e.getMessage());
            } catch (Exception ex) {
                log.error("订单明细处理失败，需手工处理，订单类型:{}，撮单信息:{}", ExchangeOrderDirection.SELL.toString(), trade);
                log.error("订单明细处理失败，需手工处理", e);
            }
        }
    }

    /**
     * 是否为机器人订单
     *
     * @param trade
     * @param direction
     * @return
     */
    private boolean isRobotOrder(ExchangeTrade trade, ExchangeOrderDirection direction) {
        return MarketUtil.isRobotOrder(trade, direction);
    }
}
