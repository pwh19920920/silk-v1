package com.spark.bitrade.service.base;


import com.alibaba.fastjson.JSONArray;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.KLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MarketBaseService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<KLine> findAllKLine(String symbol, String peroid) {
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "time"));
        Query query = new Query().with(sort).limit(1000);

        return mongoTemplate.find(query, KLine.class, "exchange_kline_" + symbol + "_" + peroid);
    }

    /**
     *  查询k线数据
     *  @author yangch
     *  @time 2018.07.19 11:27 
     *
     * @param symbol
     * @param fromTime 开始时间（大于等于）
     * @param toTime   结束时间（小于等于）
     * @param period   周期
     *                  
     */
    public List<KLine> findAllKLine(String symbol, long fromTime, long toTime, String period) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "time"));
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query, KLine.class, "exchange_kline_" + symbol + "_" + period);
        return kLines;
    }

    /**
     *   查询历史并使用缓存(缓存List结果)
     *   @author yangch
     *   @time 2018.06.28 17:08 
     *
     * @param symbol
     * @param fromTime
     * @param toTime
     * @param period
     * @param endCacheTime
     * @param periodCacheTime  
     */
    @Cacheable(cacheNames = "kline", key = "'klineTrend:'+#symbol+':'+#period+':'+#periodCacheTime+':'+#endCacheTime")
    public JSONArray findSymbolThumbWithTrendCache(String symbol, long fromTime, long toTime, String period, long endCacheTime, long periodCacheTime) {
        List<KLine> lines = findAllKLine(symbol, fromTime, toTime, period);
        JSONArray trend = new JSONArray();
        for (KLine line : lines) {
            trend.add(line.getClosePrice());
        }

        return trend;
    }

    /**
     *  查询历史并使用缓存(缓存的JSONArray结果)
     *  @author yangch
     *  @time 2018.06.27 17:28 
     *
     * @param symbol          币种
     * @param fromTime        开始时间戳
     * @param toTime          截至时间戳
     * @param period          周期
     * @param endCacheTime    k线截至的到分钟的时间戳
     * @param periodCacheTime 周期时间戳
     *                         
     */
    ///k线的缓存key=symbol+":"+resolution+":"+time(toTime-fromTime)+":"+toTime
    @Cacheable(cacheNames = "kline", key = "'kline:'+#symbol+':'+#period+':'+#periodCacheTime+':'+#endCacheTime")
    public JSONArray findKHistoryCache(String symbol, long fromTime, long toTime, String period, long endCacheTime, long periodCacheTime) {
        List<KLine> list = findAllKLine(symbol, fromTime, toTime, period);
        //edit by yangch 时间： 2018.06.07 原因：如果是1min则添加当前实时的K线
        //2018-06-08 添加以下代码会触发pc端k线频繁的访问该接口
        /*if(period.equalsIgnoreCase("1min")){
            list.add(coinProcessorFactory.getProcessor(symbol).getKLine());
        }*/

        JSONArray array = new JSONArray();
        for (KLine item : list) {
            //edit by yangch 时间： 2018.07.20 原因：代码优化
            /*JSONArray group = new JSONArray();
            group.add(0,item.getTime());
            group.add(1,item.getOpenPrice());
            group.add(2,item.highestPrice());
            group.add(3,item.lowestPrice());
            group.add(4,item.getClosePrice());
            group.add(5,item.getVolume());
            array.add(group);*/
            array.add(klineToJSONArray(item));
        }
        return array;
    }

    //add by yangch 时间： 2018.07.20 原因：
    //K线转换为JSONArray对象
    public JSONArray klineToJSONArray(KLine kLine) {
        JSONArray group = new JSONArray();
        group.add(0, kLine.getTime());
        group.add(1, kLine.getOpenPrice());
        group.add(2, kLine.getHighestPrice());
        group.add(3, kLine.getLowestPrice());
        group.add(4, kLine.getClosePrice());
        group.add(5, kLine.getVolume());

        return group;
    }

    /**
     *  指定时间段内的第一笔交易
     *  @author yangch
     *  @time 2018.07.17 11:43 
     *
     * @param symbol
     * @param fromTime
     * @param toTime    
     */
    public ExchangeTrade findFirstTrade(String symbol, long fromTime, long toTime) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "time"));
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    /**
     *  指定时间段内的最后一笔交易
     *  @author yangch
     *  @time 2018.07.17 11:43 
     *
     * @param symbol
     * @param fromTime
     * @param toTime    
     */
    public ExchangeTrade findLastTrade(String symbol, long fromTime, long toTime) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "time"));
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    //add by yangch 时间： 2018.05.03 原因：代码合并
    public ExchangeTrade findTrade(String symbol, long fromTime, long toTime, Sort.Order order) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = new Sort(order);
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }


    public List<ExchangeTrade> findTradeByTimeRange(String symbol, long timeStart, long timeEnd) {
        Criteria criteria = Criteria.where("time").gte(timeStart).andOperator(Criteria.where("time").lt(timeEnd));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "time"));
        Query query = new Query(criteria).with(sort);

        return mongoTemplate.find(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    /**
     *  保存交易数据
     *  @author yangch
     *  @time 2018.07.16 14:17 
     *
     * @param symbol        交易对名称
     * @param exchangeTrade 交易明细数据
     *                       
     */
    public void saveTrade(String symbol, ExchangeTrade exchangeTrade) {
        //保存失败 的情况未处理？？？？？？？
        mongoTemplate.insert(exchangeTrade, "exchange_trade_" + symbol);
    }

    /**
     * 查找某时间段内的交易量
     *
     * @param symbol
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public BigDecimal findTradeVolume(String symbol, long timeStart, long timeEnd) {
        Criteria criteria = Criteria.where("time").gt(timeStart)
                .andOperator(Criteria.where("time").lte(timeEnd));
        //.andOperator(Criteria.where("volume").gt(0));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "time"));
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query, KLine.class, "exchange_kline_" + symbol + "_1min");
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (KLine kLine : kLines) {
            totalVolume = totalVolume.add(kLine.getVolume());
        }
        return totalVolume;
    }

}
