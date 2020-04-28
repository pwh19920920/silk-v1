package com.spark.bitrade.service;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.KafkaTopicConstant;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.handler.MarketHandler;
import com.spark.bitrade.handler.MongoMarketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/***
  * 推送交易信息
  * @author yangch
  * @time 2018.06.30 15:22 
  */

@Service
@Slf4j
public class PushTradeMessage {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;



    /***
      * 推送最新的成交信息
      * @author yangch
      * @time 2018.06.30 15:28
     * @param trades
     */
    ///@Async
    public void pushNewestTrade(String symbol, List<ExchangeTrade> trades){
        //log.info("/topic/market/trade/{}" , symbol);
        kafkaTemplate.send(KafkaTopicConstant.MSG_NEWEST_TRADE, symbol, JSON.toJSONString(trades));
    }

    /***
      * 推送盘口信息
      * @author yangch
      * @time 2018.07.03 15:01 
     * @param plateFull 盘口信息
     */
    public void pushTradePlateFull(TradePlate plateFull){
        //log.info("/topic/market/trade-plate-full/{}" , plateFull.getSymbol());
        kafkaTemplate.send(KafkaTopicConstant.MSG_TRADE_PLATE_FULL, plateFull.getSymbol(), JSON.toJSONString(plateFull));
    }



    /***
      * 推送缩略行情
      * @author yangch
      * @time 2018.07.03 15:01 
     * @param thumb 缩略行情
     */
    public void pushCoinThumb(CoinThumb thumb){
        //log.info("/topic/market/thumb");
        kafkaTemplate.send(KafkaTopicConstant.MSG_MARKET_COIN_THUMB, thumb.getSymbol(), JSON.toJSONString(thumb));
    }


    /**
     * socket方式推送k线数据
     * @param symbol 交易对名称
     * @param kLine k线数据
     */
    public void pushKLine(String symbol, KLine kLine) {
        //log.info("/topic/market/kline/{}",symbol);
        //推送K线数据
        //messagingTemplate.convertAndSend("/topic/market/kline/"+symbol,kLine);
        kafkaTemplate.send(KafkaTopicConstant.MSG_MARKET_KLINE, symbol, JSON.toJSONString(kLine));
    }


    /***
      * 推送k线
      * @author yangch
      * @time 2018.07.03 16:01 
     * @param handlers
     * @param kLine
     */
    public void pushKLine(List<MarketHandler> handlers, KLine kLine){
        for (MarketHandler storage : handlers) {
            if(!(storage instanceof MongoMarketHandler)){
                storage.handleKLine(kLine.getSymbol(), kLine);
            }
        }
    }

}
