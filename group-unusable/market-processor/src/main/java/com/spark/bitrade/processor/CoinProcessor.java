package com.spark.bitrade.processor;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.handler.MarketHandler;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.service.MarketService;

import java.util.List;

public interface CoinProcessor {

    void setIsHalt(boolean status);
    boolean isHalt();

    /**
     * 处理新生成的交易信息
     * @param trades
     * @return
     */
    void process(List<ExchangeTrade> trades);

    /**
     * 添加存储器
     * @param storage
     */
    void addHandler(MarketHandler storage);

    CoinThumb getThumb();

    void setMarketService(MarketService service);

    void setKLineService(KLineService kLineService);

    void generateKLine(int range, int field, long time);

    KLine getKLine(String period);

    void initializeThumb();

    //add by yangch 时间： 2018.07.16 原因：初始化实时K线
    void initializeRealtimeKline();

    void autoGenerate();

    void resetThumb();

    //edit by yangch 时间： 2018.04.29 原因：合并
    //void setBtcUsdtProcessor(CoinProcessor processor);
    void setExchangeRate(CoinExchangeRate coinExchangeRate);

    //void setEthUsdtProcessor(CoinProcessor processor);
    //更新 CoinThumb 的成交量
    void update24HVolume(long time);

    void initializeUsdRate();

    //设置盘口信息
    void setTradePlate(TradePlate tradePlate);
    //获取盘口信息
    TradePlate getTradePlate(ExchangeOrderDirection direction);
    //设置盘口信息是否初始化
    void isTradePlateinitialize(boolean flag);
    boolean isTradePlateinitialize();
}
