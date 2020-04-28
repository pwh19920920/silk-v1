package com.spark.bitrade.service;

import com.spark.bitrade.config.KafkaOffsetCacheKeyConstant;
import com.spark.bitrade.constant.BusinessErrorMonitorType;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/***
  * 成交订单明细处理
 *
  * @author yangch
  * @time 2018.07.02 10:01
  */

@Service
@Slf4j
public class ProcessExchangeTradeService {
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private BusinessErrorMonitorService businessErrorMonitorService;
    @Autowired
    private PushTrade2MQ pushTradeMessage;

    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    OrderDetailAggregationService orderDetailAggregationService;

    @Autowired
    private KafkaOffsetCacheService kafkaOffsetCacheService;

    /***
      * 异步处理订单明细
      * @author yangch
      * @time 2018.07.02 10:09 
     * @param trade
     */
    @Deprecated
    @Async
    public void asyncProcessExchangeTrade(ExchangeTrade trade){
        try {
            //成交明细处理
            MessageResult result = exchangeOrderService.processExchangeTrade(trade);
            if(result.getCode() == 0 ) {
                //仅推送部分成交的订单（交易对中只有一个未成交：先查询买单是否成交，如果未成交则说明卖单已成交）
                if(trade.getBuyOrderId().equalsIgnoreCase(trade.getUnfinishedOrderId())) {
                    ExchangeOrder buyOrder = exchangeOrderService.findOne(trade.getBuyOrderId());
                    buyOrder.setTurnover(trade.getUnfinishedTradedTurnover());
                    buyOrder.setTradedAmount(trade.getUnfinishedTradedAmount());

                    //修改为异步推送部分成交订单
                    pushTradeMessage.pushOrderTrade(buyOrder);
                } else {
                    ExchangeOrder sellOrder = exchangeOrderService.findOne(trade.getSellOrderId());
                    sellOrder.setTurnover(trade.getUnfinishedTradedTurnover());
                    sellOrder.setTradedAmount(trade.getUnfinishedTradedAmount());

                    //修改为异步推送部分成交订单
                    pushTradeMessage.pushOrderTrade(sellOrder);
                }
            } else {
                log.warn("未推送/topic/market/order-trade/，订单明细处理失败，成交明细：{}",trade);
                //成交明细处理结果不对时 记录错误信息
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE, trade.toString(), result.getMessage());
            }
        } catch (Exception e){
            log.warn("订单明细处理失败，成交明细：{}",trade);
            // 成交明细处理出错后 记录到告警表中
            try {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE, trade.toString(), e.getMessage());

                //删除mongodb中的ExchangeOrderDetail文档记录
                exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
                exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖的记录

                //删除mongodb中的OrderDetailAggregation文档记录
                orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
                orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖的记录
            }catch (Exception ex) {
                log.error("成交明细处理错误，撮单信息:{}", trade);
                e.printStackTrace();
            }
        }
    }

    /**
     * 异步处理成交明细（按买单和卖单拆分处理）
     * @param trade 匹配订单
     * @param direction 订单类型
     * @param kafkaOffset kafka offset参数
     * @return
     * @throws Exception
     */
    @Async
    public void asyncProcessExchangeTrade(ExchangeTrade trade, ExchangeOrderDirection direction, long kafkaOffset){
        try {
            //成交明细处理
            MessageResult result = exchangeOrderService.processExchangeTrade(trade, direction);
            if(result.getCode() == 0 ) {
                ExchangeOrder order = (ExchangeOrder)result.getData();
                if(null == order) {
                    //重新获取订单信息
                    if(direction == ExchangeOrderDirection.BUY)  {
                        order = exchangeOrderService.findOne(trade.getBuyOrderId());
                    } else if(direction == ExchangeOrderDirection.SELL) {
                        order = exchangeOrderService.findOne(trade.getSellOrderId());
                    }
                }

                //仅推送部分成交的订单（交易对中只有一个未成交）
                if(order.getOrderId().equalsIgnoreCase(trade.getUnfinishedOrderId())) {
                    order.setTurnover(trade.getUnfinishedTradedTurnover());
                    order.setTradedAmount(trade.getUnfinishedTradedAmount());

                    pushTradeMessage.pushOrderTrade(order);
                }
            } else {
                log.warn("未推送/topic/market/order-trade/，订单明细处理失败，成交明细：{}",trade);
                //成交明细处理结果不对时 记录错误信息
                if(direction == ExchangeOrderDirection.BUY) {
                    businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY, trade.toString(), result.getMessage());
                } else {
                    businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL, trade.toString(), result.getMessage());
                }
            }
        } catch (Exception e){
            //org.hibernate.exception.JDBCConnectionException
            //org.hibernate.exception.LockTimeoutException
            processBusinessError(trade, direction, e);
        }

        //Redis系统中移除待消费的消息标识
        if(kafkaOffset >=0) {
            kafkaOffsetCacheService.removeListItem(
                    KafkaOffsetCacheKeyConstant.
                            getKeyExchangeTrade(trade.getSymbol(), direction), 1, kafkaOffset);
        }
    }

    public void processBusinessError(ExchangeTrade trade, ExchangeOrderDirection direction ,Exception e) {
        log.warn("订单明细处理失败，订单类型:{}，撮单信息:{}",direction.toString() , trade);
        // 成交明细处理出错后 记录到告警表中
        try {
            if(direction == ExchangeOrderDirection.BUY) {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_BUY, trade.toString(), e.getMessage());

                //删除mongodb中的ExchangeOrderDetail文档记录
                exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
                //删除mongodb中的OrderDetailAggregation文档记录
                orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
            } else {
                businessErrorMonitorService.add(BusinessErrorMonitorType.EXCHANGE__PROCESS_EXCHANGE_TRADE_SELL, trade.toString(), e.getMessage());

                //删除mongodb中的ExchangeOrderDetail文档记录
                exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖单记录
                //删除mongodb中的OrderDetailAggregation文档记录
                orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //单的记录
            }
        } catch (Exception ex) {
            log.error("订单明细处理失败，订单类型:{}，撮单信息:{}",direction.toString() , trade);
            e.printStackTrace();
        }
    }

    //将mongodb库中的订单信息给删除掉
    public void tryAgain(ExchangeTrade trade, ExchangeOrderDirection direction ){
        if(direction == ExchangeOrderDirection.BUY) {
            //删除mongodb中的ExchangeOrderDetail文档记录
            exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录
            //删除mongodb中的OrderDetailAggregation文档记录
            orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getBuyOrderId(), trade.getSellOrderId()); //买单记录


        } else {
            //删除mongodb中的ExchangeOrderDetail文档记录
            exchangeOrderDetailService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //卖单记录
            //删除mongodb中的OrderDetailAggregation文档记录
            orderDetailAggregationService.deleteByOrderIdAndRefOrderId(trade.getSellOrderId(), trade.getBuyOrderId()); //单的记录
        }
    }

}
