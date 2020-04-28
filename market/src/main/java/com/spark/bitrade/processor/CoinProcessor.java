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

    /**
     * 初始化
     */
    void initialize();

    /**
     * 设置盘口信息是否初始化
     *
     * @param flag
     */
    void isTradePlateinitialize(boolean flag);

    /**
     * @param service
     */
    void setMarketService(MarketService service);

    /**
     * 设置K线服务
     *
     * @param kLineService
     */
    void setKLineService(KLineService kLineService);

    /**
     * 设置汇率服务
     *
     * @param coinExchangeRate
     */
    void setExchangeRate(CoinExchangeRate coinExchangeRate);

    /**
     * 状态设置
     *
     * @param status
     */
    void setIsHalt(boolean status);

    /**
     * 获取状态
     *
     * @return
     */
    boolean isHalt();

    /**
     * 处理新生成的交易信息
     *
     * @param trades
     * @return
     */
    void process(List<ExchangeTrade> trades);

    /**
     * 添加存储器
     *
     * @param storage
     */
    void addHandler(MarketHandler storage);

    /**
     * 获取实时缩略行情
     *
     * @return
     */
    CoinThumb getThumb();

    /**
     * 生成k线数据
     *
     * @param range k线周期范围 [1,5,10,15,30]min、[1]hour、[1]week、[1]day、[1]month
     * @param field k线周期类型 已知枚举选项：min=Calendar.MINUTE、hour=Calendar.HOUR_OF_DAY、week=Calendar.DAY_OF_WEEK、day=Calendar.DAY_OF_YEAR、month=Calendar.DAY_OF_MONTH
     * @param time  k线时间
     */
    void generateKLine(int range, int field, long time);

    /**
     * 获取K线
     *
     * @param period
     * @return
     */
    KLine getKLine(String period);

    /**
     * 获取前一周期的K线
     *
     * @param period
     * @return
     */
    KLine getPrevKLine(String period);

    /**
     * 重置 缩略行情（00:00:00 时重置）
     */
    void resetThumb();

    /**
     * 更新 CoinThumb 的 最近24小时的成交量
     *
     * @param time
     */
    void update24HVolume(long time);

    /**
     * 设置盘口信息
     *
     * @param tradePlate
     */
    void setTradePlate(TradePlate tradePlate);

    /**
     * 获取盘口信息
     *
     * @param direction
     * @return
     */
    TradePlate getTradePlate(ExchangeOrderDirection direction);

    /**
     * 获取初始化状态
     *
     * @return
     */
    boolean isTradePlateinitialize();
}
