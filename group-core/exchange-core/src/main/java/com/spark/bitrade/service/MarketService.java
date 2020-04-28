package com.spark.bitrade.service;


import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.service.base.MarketBaseService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MarketService extends MarketBaseService {
    /**
     *  保存交易数据(异步保存)
     *  @author yangch
     *  @time 2018.07.16 14:17 
     *
     * @param symbol        交易对名称
     * @param exchangeTrade 交易明细数据 
     */
    @Async
    @Override
    public void saveTrade(String symbol, ExchangeTrade exchangeTrade) {
        super.saveTrade(symbol, exchangeTrade);
    }
}
