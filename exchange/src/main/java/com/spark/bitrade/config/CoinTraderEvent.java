package com.spark.bitrade.config;

import com.spark.bitrade.trader.CoinTrader;
import com.spark.bitrade.trader.CoinTraderFactory;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeOrderDetail;
import com.spark.bitrade.service.ExchangeOrderDetailService;
import com.spark.bitrade.service.ExchangeOrderService;
import com.spark.bitrade.service.ICywService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class CoinTraderEvent implements ApplicationListener<ContextRefreshedEvent> {
    private Logger log = LoggerFactory.getLogger(CoinTraderEvent.class);
    @Autowired
    CoinTraderFactory coinTraderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    private ICywService cywService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======initialize coinTrader data begin======");

        HashMap<String, CoinTrader> traders = coinTraderFactory.getTraderMap();
        traders.forEach((symbol, trader) -> {
            recoverCoinTraderData(symbol, trader);
        });

        log.info("======initialize coinTrader data completed======");
    }

    //恢复交易数据
    public void recoverCoinTraderData(String symbol, CoinTrader trader) {
        if (!trader.getReady()) {
            //机器人订单
            List<ExchangeOrder> apiOrders = cywService.openOrders(symbol);
            log.info("加载机器人订单，交易对={}，订单数量={}", symbol, apiOrders.size());
            this.recoverCoinTraderData(trader, apiOrders);

            //用户订单
            List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
            log.info("加载用户订单，交易对={}， 订单数量={}", symbol, orders.size());
            this.recoverCoinTraderData(trader, orders);

            //设置撮合器已准备就绪
            trader.setReady(true);
        }
    }

    /**
     * 恢复订单
     *
     * @param trader
     * @param orders
     */
    private void recoverCoinTraderData(CoinTrader trader, List<ExchangeOrder> orders) {
        List<ExchangeOrder> tradingOrders = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();

        orders.forEach(order -> {
            BigDecimal tradedAmount = BigDecimal.ZERO;
            BigDecimal turnover = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());
            order.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
                //edit by yangch 时间： 2018.05.21 原因：相乘后需要处理精度
                //turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
                if (trade.getTurnover() == null || trade.getTurnover().compareTo(BigDecimal.ZERO) <= 0) {
                    turnover = turnover.add(trade.getAmount().multiply(trade.getPrice())).setScale(trader.getCoinScale(), BigDecimal.ROUND_UP);
                } else {
                    turnover = turnover.add(trade.getTurnover());
                }
            }
            order.setTradedAmount(tradedAmount);
            order.setTurnover(turnover);

            if (order.isCompleted()) {
                completedOrders.add(order);
            } else {
                tradingOrders.add(order);
            }
        });
        trader.trade(tradingOrders);

        //判断已完成的订单发送消息通知
        if (completedOrders.size() > 0) {
            trader.orderCompleted(completedOrders);
        }
    }

}
