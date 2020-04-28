package com.spark.bitrade.controller;


import com.alibaba.fastjson.JSONArray;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
public class KLineController extends CommonController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;


    /**
     * 获取币种历史K线
     *
     * @param symbol
     * @param from       开始时间戳
     * @param to         截至时间戳
     * @param resolution 周期
     * @return
     */
    @RequestMapping(value = {"history", "kline/history"}, method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
    public JSONArray findKHistory(String symbol, long from, long to, String resolution) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }
        long startTime = System.currentTimeMillis();
        //根据动态的起止时间戳，计算相同周期内的缓存截至时间戳。相同周期内，相同的缓存截止时间戳内不重复查询mongodb库
        ///long endCacheTime =  toHourTime(to);                     //缓存截止时间:默认缓存时长为1小时
        ///long periodCacheTime =endCacheTime-toHourTime(from);     //查询周期，根据查询的起止时间计算查询周期

        String period = getPeriod(resolution);

        ///long currentTime = KLineService.getCurrentPeriodStartTime(new Date().getTime(), period);
        long currentTime = KLineService.getCurrentPeriodStartTime(System.currentTimeMillis(), period);
        //缓存截止时间
        long endCacheTime = KLineService.getCurrentPeriodStartTime(to, period);
        //long toTime = KLineService.getCurrentPeriodStartTime(to, period);
        //查询周期，根据查询的起止时间计算查询周期
        long periodCacheTime = endCacheTime - KLineService.getCurrentPeriodStartTime(from, period);

        //获取历史K线数据
        JSONArray array = marketService.findKHistoryCache(symbol, from, to, period, endCacheTime, periodCacheTime);

        //处理最后一条K线数据为实时数据（但要考虑 查询的是不是最近时间段的K线，如PC端tradingview插件会 访问其他历史时间段的数据）
        //查询的提供的结束时候和系统当前时间一致的情况下，考虑添加最新的K线实时数据
        if (currentTime == endCacheTime) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
            KLine currentKLine = processor.getKLine(period);
            if (currentKLine != null) {
                //判断最后一条K线数据是否重复了，如果和实时的重复了 则删除
                if (array.size() > 0) {
                    int lastInex = array.size() - 1;
                    long lastTime = array.getJSONArray(lastInex).getLong(0);
                    if (lastTime == currentKLine.getTime()) {
                        array.remove(lastInex);
                        log.warn("交易对={}，周期={}，时间={}， 历史K线数据和实时K线数据重复", symbol, period, String.valueOf(currentKLine.getTime()));
                    }
                }

                //添加最新的实时K线数据
                array.add(marketService.klineToJSONArray(currentKLine));
            } else {
                log.warn("{}交易对中{}周期的实时K线数据不存在", symbol, period);
            }
        }

        log.info("market history user times:{} ms", (System.currentTimeMillis() - startTime));
        return array;
    }


    /**
     * 获取实时K线
     *
     * @param symbol
     * @param resolution 周期
     * @return
     */
    @RequestMapping(value = "kline/realtime", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
    public JSONArray realtimeKline(String symbol, String resolution) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }

        String period = getPeriod(resolution);
        JSONArray array = new JSONArray();

        CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
        if (processor != null) {
            KLine currentKLine = processor.getKLine(period);
            if (currentKLine != null) {
                //添加最新的实时K线数据
                array.add(marketService.klineToJSONArray(currentKLine));
            } else {
                log.warn("{}交易对中{}周期的实时K线数据不存在", symbol, period);
            }
        }
        return array;
    }

    /**
     * 获取最近的2条K线
     *
     * @param symbol
     * @param resolution 周期
     * @return
     */
    @RequestMapping(value = "kline/newest2", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
    public JSONArray newest2Kline(String symbol, String resolution) {
        if (null != symbol) {
            symbol = symbol.toUpperCase();
        }

        String period = getPeriod(resolution);
        JSONArray array = new JSONArray();

        CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
        if (processor != null) {
            KLine currentKLine = processor.getKLine(period);
            KLine prevKLine = processor.getPrevKLine(period);
            if (Objects.isNull(prevKLine)) {
                log.info("{}交易对中{}周期的无前一周期K线数据", symbol, period);
                prevKLine = currentKLine;
            }

            if (currentKLine != null) {
                //添加最新的实时K线数据
                array.add(marketService.klineToJSONArray(currentKLine));
                array.add(marketService.klineToJSONArray(prevKLine));
            } else {
                log.warn("{}交易对中{}周期的实时K线数据不存在", symbol, period);
            }
        }
        return array;
    }

    /**
     * @param resolution 周期
     * @return
     */
    private String getPeriod(String resolution) {
        String period;
        if (resolution.endsWith("H") || resolution.endsWith("h")) {
            period = resolution.substring(0, resolution.length() - 1) + "hour";
        } else if (resolution.endsWith("D") || resolution.endsWith("d")) {
            period = resolution.substring(0, resolution.length() - 1) + "day";
        } else if (resolution.endsWith("W") || resolution.endsWith("w")) {
            period = resolution.substring(0, resolution.length() - 1) + "week";
        } else if (resolution.endsWith("M") || resolution.endsWith("m")) {
            period = resolution.substring(0, resolution.length() - 1) + "month";
        } else {
            Integer val = Integer.parseInt(resolution);
            if (val < 60) {
                period = resolution + "min";
            } else {
                period = (val / 60) + "hour";
            }
        }
        return period;
    }

}
