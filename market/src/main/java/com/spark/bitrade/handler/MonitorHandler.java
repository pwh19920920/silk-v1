package com.spark.bitrade.handler;

import com.spark.bitrade.consumer.TradeMessageDataManager;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.service.LatestTradeCacheService;
import com.spark.bitrade.service.LockCoinRechargeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
  * 监控类
 *
  * @author yangch
  * @time 2018.08.02 11:12
  */
@Slf4j
@Component
public class MonitorHandler implements MarketHandler {
    @Autowired
    private LockCoinRechargeMonitorService lockCoinRechargeMonitorService;
    @Autowired
    private TradeMessageDataManager tradeMessageDataManager;

    @Autowired
    private LatestTradeCacheService latestTradeCacheService;

    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {

        //推送缩略行情
        tradeMessageDataManager.pushCoinThumb(symbol, thumb);

        //推送实时成交数据
        tradeMessageDataManager.pushLatestTrade(symbol, exchangeTrade);

        //缓存最新的实时成交数据
        latestTradeCacheService.offer(symbol, exchangeTrade);

        //自动解锁任务
        lockCoinRechargeMonitorService.monitorTask(thumb);
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        //推送k线数据
        tradeMessageDataManager.pushKLine(symbol, kLine);
    }
}
