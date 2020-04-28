package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.util.MarketUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处理订单
 *
 * @author yangch
 * @time 2018.08.13 17:05  
 */
@Service
@Slf4j
public class ProcessOrderService {
    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;

    /**
     * 分发已完成的订单
     *
     * @param record kafka消费记录
     */
    @Async("order")
    public void asyncProcessOrderComplated(ConsumerRecord<String, String> record) {
        log.info("value={}", record.value());

        List<ExchangeOrder> orders = JSON.parseArray(record.value(), ExchangeOrder.class);
        for (ExchangeOrder order : orders) {
            try {
                // 推送已完成订单任务
                if (MarketUtil.isRobotOrder(order.getOrderId())) {
                    //log.info("机器人已成交订单：{}", order);
                    kafkaTemplate.send("exchange-cyw-order-completed", order.getSymbol(), JSON.toJSONString(order));
                } else {
                    //log.info("用户已成交订单：{}：{}", order);
                    kafkaTemplate.send("exchange-user-order-completed", order.getSymbol(), JSON.toJSONString(order));
                }
            } catch (Exception e) {
                // 内存中撮单成功后，任务分发失败 ，记录到业务监控表里
                try {
                    businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__SEND_COMPLETED, order.toString(), e.getMessage());
                } catch (Exception ex) {
                    log.error("处理委托成交完成处理出错，订单ID=" + order.getOrderId(), e);
                    e.printStackTrace();
                }
            }
        }

        //Redis系统中移除待消费的消息标识
        kafkaOffsetCacheService.removeListItem(
                KafkaOffsetCacheKeyConstant.getKeyExchangeOrderCompleted(record.key()), 1, record.offset());
    }
}
