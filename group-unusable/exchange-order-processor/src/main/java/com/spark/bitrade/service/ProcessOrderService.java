package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/***
  * 处理订单
 *
  * @author yangch
  * @time 2018.08.13 17:05
  */
@Service
@Slf4j
public class ProcessOrderService {

    @Autowired
    private ExchangeOrderService exchangeOrderService;

    @Autowired
    private PushTrade2MQ pushTradeMessage;

    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;

    /**
     * 异步处理完成的订单
     *
     * @param record kafka消费记录
     */
    @Async
    public void asyncProcessOrderComplated(ConsumerRecord<String,String> record){
        log.info("topic={},key={},value={}",record.topic(),record.key(),record.value());
        //String symbol = record.key();
        MessageResult result;
        List<ExchangeOrder> orders = JSON.parseArray(record.value(), ExchangeOrder.class);
        for(ExchangeOrder order:orders) {
            //委托成交完成处理
            try {
                //result = exchangeOrderService.tradeCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                result = exchangeOrderService.tradeCompleted(order, BooleanEnum.IS_FALSE);
                /*if(result.getCode() != 0 ) {
                    businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__TRADE_COMPLETED, order.toString(), result.getMessage());
                }*/

                //推送订单成交
                if(result.getCode()==0) {
                    pushTradeMessage.pushOrderCompleted(order);
                } else {
                    log.warn("未推送/topic/market/order-completed/，order={}",order);
                }
            }catch (Exception e){
                //内存中撮单成功后，数据库状态未同步（订单状态持久化更新失败的情况，记录到业务监控表里）
                try{
                    ExchangeOrder orderNew = exchangeOrderService.findOne(order.getOrderId());
                    if (orderNew.getStatus() == ExchangeOrderStatus.TRADING) {
                        businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__TRADE_COMPLETED, order.toString(), e.getMessage());
                    } else {
                        log.error("处理委托成交完成处理出错，订单ID={}，错误信息：{}", order.getOrderId(), e.getMessage());
                    }
                }catch (Exception ex){
                    log.error("处理委托成交完成处理出错，订单ID={}，错误信息：{}", order.getOrderId(), e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        //Redis系统中移除待消费的消息标识
        kafkaOffsetCacheService.removeListItem(
                KafkaOffsetCacheKeyConstant.getKeyExchangeOrderCompleted(record.key()),1, record.offset());
    }

    /**
     * 异步处理撤销的订单
     *
     * @param record kafka消费记录
     */
    @Async
    public void asyncProcessOrderCancelSccess(ConsumerRecord<String,String> record){
        log.info("normal:topic={},key={},value={}", record.topic(), record.key(), record.value());
        //String symbol = record.key();
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);

        try {
            MessageResult result = exchangeOrderService.cancelOrder(order);
            if(result.getCode()==0) {
                //推送撤销订单
                pushTradeMessage.pushOrderCanceled(order);
            }
        }catch (Exception ex){
            try{
                //撤单报错的情况下，考虑将订单退回给撮单信息
                ExchangeOrder orderNew = exchangeOrderService.findOne(order.getOrderId());
                if (orderNew.getStatus() == ExchangeOrderStatus.TRADING) {
                    log.warn("撤单失败，订单被退回到撮单系统中。订单信息：{}", order);
                    //将撤销的订单还回到撮单系统中
                    kafkaTemplate.send("exchange-order", order.getSymbol(), JSON.toJSONString(order));
                } else {
                    log.error("撤单失败，订单ID={}，错误信息：{}", order.getOrderId(), ex.getMessage());
                }
            }catch (Exception e){
                log.error("撤单失败，订单信息："+record.value());
                ex.printStackTrace();
            }
        }
    }

    /**
     * 异步处理撤销的订单(不在内存交易队列中)
     *
     * @param record kafka消费记录
     */
    @Async
    public void asyncProcessOrderCancelFail(ConsumerRecord<String,String> record){
        log.info("abnormality:topic={},key={},value={}", record.topic(), record.key(), record.value());
        //String symbol = record.key();
        ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
        try {
            MessageResult result = exchangeOrderService.cancelOrderNotInExchange(order);
            if (result.getCode() == 0) {
                //推送撤销订单
                pushTradeMessage.pushOrderCanceled(order);
            }
        }catch (Exception ex){
            log.error("撤单失败，订单信息:："+record.value());
        }
    }


}
