package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.service.OrderUtil;
import com.spark.bitrade.trader.CoinTrader;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 快速消费MQ的订单
 *
 * @author yangch
 * @time 2018.08.17 9:45  
 */
@Slf4j
@Component
public class ExchangeOrderCacheAndConsumer implements DisposableBean {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private boolean runnable = true;

    /**
     * 缓存MQ推送的订单
     */
    private static Map<String, BlockingQueue<ConsumerRecord<String, String>>> consumerCache = new HashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 初始化资源
     *
     * @param exchangeCoin
     * @param trader
     */
    public void initExchangeOrderCacheAndConsumer(ExchangeCoin exchangeCoin, CoinTrader trader) {
        log.info("初始化订单消费队列及线程，symbol={}", exchangeCoin.getSymbol());

        if (consumerCache.get(exchangeCoin.getSymbol()) == null) {
            if (exchangeCoin.getSymbol().equalsIgnoreCase(trader.getSymbol())) {
                // 初始化缓存队列
                consumerCache.put(exchangeCoin.getSymbol(), new LinkedBlockingQueue());

                // 初始化 消费线程
                executor.submit(new ExchangeOrderConsumerThread(exchangeCoin.getSymbol(), trader));
            } else {
                log.error("初始化订单消费队列及线程失败，交易对不匹配。ExchangeCoin={}, trader Symbol ={}", exchangeCoin, trader.getSymbol());
            }
        }
    }

    /**
     * 添加成交明细到队列中
     *
     * @param record
     */
    public void put2OrderConsumerQueue(ConsumerRecord<String, String> record) {
        try {
            BlockingQueue<ConsumerRecord<String, String>> queue = consumerCache.get(record.key());
            if (queue == null) {
                log.warn("该交易对的缓存队列不存在，自动取消订单,symbol={}, record={}", record.key(), record.value());
                // 该交易对的缓存队列不存在，撤销订单
                ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
                if (null != order && null != order.getOrderId()) {
                    // 撮合器未准备就绪，撤销订单
                    this.cancelOrder(order);
                }
                return;
            }

            ///log.debug("putOrder:symbol={}, value={}", record.key(), record.value());
            queue.put(record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cancelOrder(ExchangeOrder order) {
        //（？ 此处的撤单还需要进一步考虑，使用“exchange-order-cancel-success”或“exchange-order-cancel-fail”）
        if (OrderUtil.isRobotOrder(order.getOrderId())) {
            kafkaTemplate.send("exchange-cyw-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
        } else {
            kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
        }
    }

    @Override
    public void destroy() throws Exception {
        runnable = false;
    }

    /**
     * 订单消费线程
     */
    public class ExchangeOrderConsumerThread implements Runnable {
        private final String symbol;
        private final CoinTrader trader;


        public ExchangeOrderConsumerThread(String symbol, CoinTrader trader) {
            this.symbol = symbol;
            this.trader = trader;
        }

        @Override
        public void run() {
            //消费缓存队列的订单
            BlockingQueue<ConsumerRecord<String, String>> queue = consumerCache.get(symbol);
            if (null == queue) {
                log.warn("consumerOrder:{}交易对的缓存队列不存在，线程初始化失败", symbol);
                return;
            }

            while (runnable) {
                try {
                    // 判断是否退出撮合器
                    if (isTradingStop(queue)) {
                        break;
                    }

                    // 获取订单
                    ExchangeOrder order = JSON.parseObject(queue.take().value(), ExchangeOrder.class);

                    // 校验订单
                    if (!verifyOrder(order)) {
                        continue;
                    }

                    // 如果当前币种交易暂停会自动取消订单
                    if (trader.isTradingHalt() || !trader.getReady()) {
                        log.warn("当前币种交易暂停, 自动撤销订单,symbol={}, order={}", symbol, order);

                        // 撮合器未准备完成，撤回当前等待的订单
                        cancelOrder(order);
                    } else {
                        try {
                            long startTick = System.currentTimeMillis();
                            trader.trade(order);
                            log.info("complete trade, orderId={}, {}ms used!", order.getOrderId(), System.currentTimeMillis() - startTick);
                        } catch (Exception e) {
                            log.error("====交易出错，退回订单===, symbol={}, order={}", symbol, order);
                            log.error("====交易出错，退回订单===", e);
                            kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
                        }
                    }
                } catch (Exception e) {
                    log.error("订单交易出错，交易对=" + symbol, e);
                }
            }
        }

        /**
         * 判断是否退出撮合器
         *
         * @param queue
         * @return
         */
        private boolean isTradingStop(BlockingQueue<ConsumerRecord<String, String>> queue) {
            if (trader.isTradingStop()) {
                log.info("exit consumerOrder:即将退出撮合器（{}），缓存队列长度={}", symbol, queue.size());
                if (queue.size() == 0) {
                    // 退出撮合器
                    log.info("exit consumerOrder:退出撮合器（{}）", symbol);

                    // 清理资源
                    consumerCache.remove(symbol);

                    // 撤销所有订单（手动调用接口可撤销所有订单）
                    ///trader.cancelAllOrder();

                    // 退出循环
                    return true;
                }
            }
            return false;
        }

        /**
         * 校验订单
         *
         * @param order
         * @return true=校验通过/false=校验未通过
         */
        private boolean verifyOrder(ExchangeOrder order) {
            if (null == order || null == order.getOrderId()) {
                log.warn("consumerOrder:订单为空，交易对={}，订单={}", symbol, order);
                return false;
            }

            if (!symbol.equalsIgnoreCase(order.getSymbol())) {
                log.warn("交易对不匹配, 重新发送订单消息。symbol={}, order={}", symbol, order);
                // 发送消息至Exchange系统
                kafkaTemplate.send("exchange-order", order.getSymbol(), JSON.toJSONString(order));
                return false;
            }
            return true;
        }
    }
}
