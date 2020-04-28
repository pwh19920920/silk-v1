package com.spark.bitrade.handler;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.service.PushTradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebsocketMarketHandler implements MarketHandler {
    @Autowired
    private PushTradeMessage pushTradeMessage;

    /**
     * 推送币种简化信息
     *
     * @param symbol
     * @param exchangeTrade
     * @param thumb
     */
    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        //推送缩略行情
        ///messagingTemplate.convertAndSend("/topic/market/thumb",thumb);
        pushTradeMessage.pushCoinThumb4Socket(thumb);
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        //推送K线数据
        log.info("symbol={},kline={}", symbol, kLine);
        ///messagingTemplate.convertAndSend("/topic/market/kline/"+symbol,kLine);
        pushTradeMessage.pushKLine4Socket(symbol, kLine);
    }
}
