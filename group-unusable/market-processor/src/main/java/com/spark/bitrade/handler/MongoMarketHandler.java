package com.spark.bitrade.handler;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoMarketHandler implements MarketHandler {
    //@Autowired
    //private MongoTemplate mongoTemplate;

    @Autowired
    private MarketService marketService;
    @Autowired
    private KLineService kLineService;

    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        //delete by yangch 时间： 2018.05.11 原因：代码合并
        /*if(StringUtils.isNotEmpty(exchangeTrade.getSellOrderId())) {
            exchangeTrade.setBuyOrderId(null);
            exchangeTrade.setSellOrderId(null);
        }*/

        //edit by yangch 时间： 2018.07.16 原因：修改为调用service层的接口
        //mongoTemplate.insert(exchangeTrade, "exchange_trade_" + symbol);
        marketService.saveTrade(symbol, exchangeTrade);
    }

    public void handleKLine(String symbol,KLine kLine) {
        //edit by yangch 时间： 2018.07.16 原因：修改为调用service层的接口
        //mongoTemplate.insert(kLine,"exchange_kline_"+symbol+"_"+kLine.getPeriod());
        kLineService.saveKLine(symbol, kLine);
    }
}
