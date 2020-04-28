package com.spark.bitrade.mq;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.TradePlate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 *  盘口推送
 *
 * @author young
 * @time 2019.09.18 14:53
 */
@Slf4j
@Component
public class PlateMessageWrapper implements InitializingBean, DisposableBean {
    private String topic = "exchange-trade-plate";
    private boolean runnable = true;
    private BlockingQueue<PlateDelayTask> queue = new DelayQueue<>();

    private Map<String, String> plateBuy = new HashMap<>();
    private Map<String, String> plateSell = new HashMap<>();

    /**
     * 合并盘口消息并延迟推送的延迟时间，单位为毫秒，默认200毫秒
     */
    @Value("${task.plate.delayTime:200}")
    private long delayTime;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void push(TradePlate plate) {
        this.push(plate.getSymbol(), plate.getDirection(), plate.toJSONString());
    }

    /**
     * 推送盘口消息
     *
     * @param symbol    交易对
     * @param direction 盘口方向
     * @param plate     盘口数据
     */
    public void push(String symbol, ExchangeOrderDirection direction, String plate) {
        ///log.info("更新盘口数据：{},{}", symbol, direction);

        // 更新推送的盘口内容
        if (direction == ExchangeOrderDirection.BUY) {
            this.plateBuy.put(symbol, plate);
        } else {
            this.plateSell.put(symbol, plate);
        }

        // 添加延迟推送任务
        PlateDelayTask task = new PlateDelayTask(this.delayTime, symbol, direction);
        if (!this.queue.contains(task)) {
            ///log.info("添加推送任务：{}", task);
            this.queue.add(task);
        }
    }

    @Override
    public void destroy() throws Exception {
        runnable = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread thread = new Thread(this::execute);
        thread.setName("MsgSender");
        thread.setDaemon(true);
        thread.start();
    }

    private void execute() {
        while (runnable) {
            try {
                PlateDelayTask take = queue.take();
                ///log.info("延迟推送任务：{}", take);
                if (take.getDirection() == ExchangeOrderDirection.BUY) {
                    this.push2kafka(take.getSymbol(), this.plateBuy.remove(take.getSymbol()));
                } else {
                    this.push2kafka(take.getSymbol(), this.plateSell.remove(take.getSymbol()));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void push2kafka(String symbol, String message) {
        if (message != null) {
            kafkaTemplate.send(this.topic, symbol, message);
        }
    }
}
