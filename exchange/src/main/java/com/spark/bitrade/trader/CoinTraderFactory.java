package com.spark.bitrade.trader;

import com.spark.bitrade.mq.PlateMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import com.spark.bitrade.consumer.ExchangeOrderCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;

@Slf4j
public class CoinTraderFactory {


    private HashMap<String, CoinTrader> traderMap;

    public CoinTraderFactory() {
        traderMap = new HashMap<>();
    }

    public void addTrader(String symbol, CoinTrader trader) {
        traderMap.put(symbol, trader);
    }

    public CoinTrader getTrader(String symbol) {
        return traderMap.get(symbol);
    }

    public HashMap<String, CoinTrader> getTraderMap() {
        return traderMap;
    }

    /**
     * 上线交易队
     *
     * @param coin                          交易对信息
     * @param exchangeOrderCacheAndConsumer 交易对订单消费类
     * @param kafkaTemplate                 kafka接口
     * @param plateMessageWrapper           盘口推送接口
     */
    public void onlineTrader(ExchangeCoin coin,
                             ExchangeOrderCacheAndConsumer exchangeOrderCacheAndConsumer,
                             KafkaTemplate<String, String> kafkaTemplate,
                             PlateMessageWrapper plateMessageWrapper) {
        CoinTrader nowCoinTrader = getTrader(coin.getSymbol());
        if (null == nowCoinTrader) {
            CoinTrader trader = new CoinTrader(coin.getSymbol());
            trader.setKafkaTemplate(kafkaTemplate);
            trader.setPlateMessageWrapper(plateMessageWrapper);
            trader.setBaseCoinScale(coin.getBaseCoinScale());
            trader.setCoinScale(coin.getCoinScale());
            ///trader.stopTrading();

            this.addTrader(coin.getSymbol(), trader);
            log.info("new trader completed, symbol={}", coin.getSymbol());

            //初始化 订单消费的线程
            exchangeOrderCacheAndConsumer.initExchangeOrderCacheAndConsumer(coin, trader);
            log.info("=====initExchangeOrderConsumer({}) completed====", coin.getSymbol());
        } else {
            if (nowCoinTrader.isTradingHalt()) {
                //更新精度
                if (nowCoinTrader.getBaseCoinScale() != coin.getBaseCoinScale()) {
                    nowCoinTrader.setBaseCoinScale(coin.getBaseCoinScale());
                }
                if (nowCoinTrader.getCoinScale() != coin.getCoinScale()) {
                    nowCoinTrader.setCoinScale(coin.getCoinScale());
                }

                //恢复交易
                nowCoinTrader.resumeTrading();
            }
            log.warn("the trader already exist, resume trading, symbol={}", coin.getSymbol());
        }
    }


    /**
     * 下线撮合器
     *
     * @param symbol
     */
    public void offlineTrader(String symbol) {
        log.info("exit consumerOrder:命已接收到退出令,symbol={}", symbol);
        CoinTrader trader = traderMap.get(symbol);
        if (null != trader) {
            trader.stopTrading();
            traderMap.remove(symbol);
            log.info("exit consumerOrder:正在等待退出（需要订单触发）,symbol={}", symbol);
        }
    }

    /**
     * 暂停交易,不接收新的订单
     *
     * @param coin 交易对信息
     */
    public void haltTrading(ExchangeCoin coin) {
        CoinTrader nowCoinTrader = getTrader(coin.getSymbol());
        if (null != nowCoinTrader) {
            nowCoinTrader.haltTrading();
        }
    }

}
