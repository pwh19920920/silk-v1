package com.spark.bitrade.service;

import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.ext.LimitQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 缓存最近成交的数据
 * @author yangch
 * @time 2018.08.20 12:06
 */

@Service
@Slf4j
public class LatestTradeCacheService {
    @Autowired
    private ExchangeTradeService exchangeTradeService;

    //缓存记录数
    private final int cacheSize = 200;
    Map<String, LatestTradeCache> LatestTradeCacheMap;

    public LatestTradeCacheService(){
        LatestTradeCacheMap = new ConcurrentHashMap<>();
    }

    public void checkSymbol(String symbol){
        if(!LatestTradeCacheMap.containsKey(symbol)){
            LatestTradeCacheMap.put(symbol, new LatestTradeCache());
        }
    }

    public void initCacheQueue(String symbol){
        List<ExchangeTrade> list = exchangeTradeService.findLatest(symbol, cacheSize);
        Collections.reverse(list); //查询的数据为倒序排序，队列按顺序存储
        initCacheQueue( symbol, list);
    }

    public void initCacheQueue(String symbol, List<ExchangeTrade> list){
        checkSymbol(symbol);
        LatestTradeCacheMap.get(symbol).initCacheQueue(list);
    }

    public void offer(String symbol, ExchangeTrade exchangeTrade){
        LatestTradeCacheMap.get(symbol).offer(exchangeTrade);
    }

    public List<ExchangeTrade> pollAll(String symbol){
        return LatestTradeCacheMap.get(symbol).pollAll();
    }

    public List<ExchangeTrade> poll(String symbol, int size){
        return LatestTradeCacheMap.get(symbol).poll(size);
    }

    public int getCacheSize(){
        return this.cacheSize;
    }

    //缓存最新成交明细
    public class LatestTradeCache{
        //缓存队列
        private LimitQueue<ExchangeTrade> cacheQueue;

        public LatestTradeCache(){
            cacheQueue = new LimitQueue(cacheSize);
        }

        /**
         * 初始化缓存的队列
         * @param list
         */
        public void initCacheQueue(List<ExchangeTrade> list){
            if(null != list){
                list.forEach(exchangeTrade -> cacheQueue.offer(exchangeTrade));
            }
        }

        public void offer(ExchangeTrade exchangeTrade){
            cacheQueue.offer(exchangeTrade);
        }

        public List<ExchangeTrade> pollAll(){
            return cacheQueue.getAll();
        }

        public List<ExchangeTrade> poll(int size){
            List<ExchangeTrade> list =null;
            if(cacheQueue.size()>size){
                //数据的结果为倒序排序
                list= cacheQueue.getAll().subList(cacheQueue.size()-size, cacheQueue.size());
            } else {
                list= cacheQueue.getAll();
            }
            //最新成交数据为倒序排序
            if(null!=list){
                Collections.reverse(list);
            }

            return list;
        }
    }
}
