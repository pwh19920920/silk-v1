package com.spark.bitrade.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
public class CoinCacheProcessorFactory {
    private HashMap<String,CoinCacheProcessor> processorMap; //yangch：按交易对存放

    public CoinCacheProcessorFactory(){
        processorMap = new HashMap<>();
    }

    public void addProcessor(String symbol, CoinCacheProcessor processor){
        log.info("CoinProcessorFactory addProcessor = {}" , symbol);
        processorMap.put(symbol,processor);
    }

    public CoinCacheProcessor getProcessor(String symbol){
        return processorMap.get(symbol);
    }

    public HashMap<String, CoinCacheProcessor> getProcessorMap() {
        return processorMap;
    }

    public void removeProcessor(String symbol){
        processorMap.remove(symbol);
    }
}
