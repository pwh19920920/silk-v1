package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.trader.CoinTrader;
import com.spark.bitrade.trader.CoinTraderFactory;
import com.spark.bitrade.config.CoinTraderEvent;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.mq.PlateMessageWrapper;
import com.spark.bitrade.service.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ExchangeOrderConsumer {
    @Autowired
    private CoinTraderFactory traderFactory;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ExchangeOrderCacheAndConsumer exchangeOrderCacheAndConsumer;
    @Autowired
    private CoinTraderEvent coinTraderEvent;
    @Autowired
    private PlateMessageWrapper plateMessageWrapper;

    /**
     * 消费订单
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-order", group = "group-handle")
    public void onOrderSubmitted(ConsumerRecord<String, String> record) {
        exchangeOrderCacheAndConsumer.put2OrderConsumerQueue(record);

//        log.info("onOrderSubmitted:topic={},key={}",record.topic(),record.key());
//        String symbol = record.key();
//        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
//        if(order == null){
//            return ;
//        }
//        CoinTrader trader = traderFactory.getTrader(symbol);
//        //如果当前币种交易暂停会自动取消订单
//        if(trader.isTradingHalt() || !trader.getReady()){
//            //撮合器未准备完成，撤回当前等待的订单
//            kafkaTemplate.send("exchange-order-cancel-success",order.getSymbol(), JSON.toJSONString(order));
//        } else{
//            try {
//                long startTick = System.currentTimeMillis();
//                trader.trade(order);
//                log.info("complete trade,orderId={},{}ms used!",order.getOrderId(), System.currentTimeMillis() - startTick);
//            } catch (Exception e){
//                e.printStackTrace();
//                log.error("====交易出错，退回订单===");
//                kafkaTemplate.send("exchange-order-cancel-success",order.getSymbol(), JSON.toJSONString(order));
//            }
//        }
    }

    /**
     * 消费撤单
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-order-cancel", group = "group-handle")
    public void onOrderCancel(ConsumerRecord<String, String> record) {
        String symbol = record.key();
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        if (order == null) {
            return;
        }
        log.info("onOrderCancel:symbol={},orderId={}", symbol, order.getOrderId());

        CoinTrader trader = traderFactory.getTrader(symbol);

        //edit by yangch 时间： 2018.05.11 原因：代码合并
        ExchangeOrder result = null;
        if (trader != null && trader.getReady()) {
            //解决交易对下架的情况
            result = trader.cancelOrder(order);
        } else {
            log.warn("撮合器无效，撤销订单，订单号={}", order.getOrderId());
        }
        // edit by yangch 时间： 2018.05.21 原因：修复订单不在交易内存中的情况
        if (result == null && order.getStatus() != ExchangeOrderStatus.TRADING) {
            log.warn("撮合器队列中没有该订单，且订单状态不为“交易中”，订单号={}", order.getOrderId());
            return;
        } else {
            if (result == null) {
                // edit by yangch 时间： 2018.05.22 原因：处理部分订单没有在交易队列中，但交易状态有为正在交易的情况
                result = order;
                log.warn("撮合器队列中没有该订单，订单号={}", order.getOrderId());
                ///设置交易数量和交易额都为0，进行重新计算
                //result.setTurnover(BigDecimal.valueOf(0));
                //result.setTradedAmount(BigDecimal.valueOf(0));

                // 交易内存队列中不存在的撤单
                if (OrderUtil.isRobotOrder(order.getOrderId())) {
                    kafkaTemplate.send("exchange-cyw-order-cancel-fail", order.getSymbol(), JSON.toJSONString(result));
                } else {
                    kafkaTemplate.send("exchange-order-cancel-fail", order.getSymbol(), JSON.toJSONString(result));
                }
            } else {
                // 正常的从交易内存队列中撤单
                if (OrderUtil.isRobotOrder(order.getOrderId())) {
                    kafkaTemplate.send("exchange-cyw-order-cancel-success", order.getSymbol(), JSON.toJSONString(result));
                } else {
                    kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(result));
                }
            }
        }
    }

    /**
     * 管理指定的交易对撮合器
     *
     * @param record
     */
    @KafkaListener(topics = "exchange-trader-manager", group = "group-handle")
    public void onCoinTraderManager(ConsumerRecord<String, String> record) {
        log.info("onCoinTraderManager:topic={},key={}", record.topic(), record.key());
        String symbol = record.key();
        ExchangeCoin exchangeCoin = JSON.parseObject(record.value(), ExchangeCoin.class);
        if (exchangeCoin == null) {
            return;
        }

        if (exchangeCoin.getEnable() == 1) {
            //启用
            traderFactory.onlineTrader(exchangeCoin, exchangeOrderCacheAndConsumer, kafkaTemplate, plateMessageWrapper);

            coinTraderEvent.recoverCoinTraderData(exchangeCoin.getSymbol(), traderFactory.getTrader(symbol));
        } else if (exchangeCoin.getEnable() == 2) {
            //禁用
            traderFactory.haltTrading(exchangeCoin);
        } else {
            log.warn("onCoinTraderManager:命令错误!! topic={},key={}", record.topic(), record.key());
        }

        kafkaTemplate.send(KafkaTopicConstant.exchangeProcessorManager, exchangeCoin.getSymbol(), JSON.toJSONString(exchangeCoin));
    }

}
