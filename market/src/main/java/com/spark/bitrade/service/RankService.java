package com.spark.bitrade.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

/**
 *  排名服务
 *
 * @author young
 * @time 2019.11.28 17:24
 */
@Slf4j
@Service
public class RankService {
    @Autowired
    private KLineService kLineService;

    /**
     * 默认统计数据
     */
    private RankDependData defaultData = new RankDependData(0, BigDecimal.ZERO);

    /**
     * 创建缓存，默认1分钟过期
     */
    private Cache<String, RankDependData> cachedRankDependData = CacheUtil.newTimedCache(DateUnit.MINUTE.getMillis());

    /**
     * 获取排名依赖的交易量统计数据
     *
     * @param symbol 交易对
     * @return
     */
    public RankDependData getRankDependData(final String symbol) {
        // 规则： 60分钟交易数量大于XX且大于x笔交易，才能参与首页排名
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();

        return getRankDependData(symbol, time);
    }

    /**
     * 获取排名依赖的交易量统计数据
     *
     * @param symbol  交易对
     * @param endTime 1min k线的截止时间戳
     * @return
     */
    private RankDependData getRankDependData(String symbol, long endTime) {
        RankDependData rankDependData = cachedRankDependData.get(this.getcachedKey(symbol, endTime));

        if (rankDependData != null) {
            return rankDependData;
        }

        // 获取实时数据
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endTime);
        // 提前60分钟
        calendar.add(Calendar.MINUTE, -60);
        long fromTime = calendar.getTimeInMillis();
        Map<String, BigDecimal> map = kLineService.getStatsFromKLine(symbol, "1min", fromTime, endTime, true);
        if (map != null && map.size() > 0) {
            rankDependData = new RankDependData(map.getOrDefault("count", BigDecimal.ZERO).intValue(),
                    map.getOrDefault("volume", BigDecimal.ZERO));
        } else {
            rankDependData = defaultData;
        }

        // 性能考虑，缓存数据
        cachedRankDependData.put(this.getcachedKey(symbol, endTime), rankDependData);

        return rankDependData;
    }

    /**
     * 获取缓存的key
     *
     * @param symbol
     * @param time
     * @return
     */
    private String getcachedKey(String symbol, long time) {
        return new StringBuilder(symbol).append(time).toString();
    }

    @Data
    public static class RankDependData {
        /**
         * 交易笔数
         */
        private Integer tradeTimes;

        /**
         * 交易成数量
         */
        private BigDecimal tradeAmount;

        public RankDependData(Integer tradeTimes, BigDecimal tradeAmount) {
            this.tradeTimes = tradeTimes;
            this.tradeAmount = tradeAmount;
        }
    }
}
