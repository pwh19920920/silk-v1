package com.spark.bitrade.component;

import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.handler.MongoMarketHandler;
import com.spark.bitrade.handler.MonitorHandler;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import com.spark.bitrade.processor.DefaultCoinProcessor;
import com.spark.bitrade.service.KLineService;
import com.spark.bitrade.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * 
 * @author yangch
 * @time 2018.08.24 16:31
 */

@Component
@Slf4j
public class CoinProcessorManager {
    @Autowired
    private MongoMarketHandler mongoMarketHandler;
    //@Autowired
    //private WebsocketMarketHandler wsHandler;

    @Autowired
    private MarketService marketService;
    @Autowired
    private MonitorHandler monitorHandler;
    @Autowired
    private CoinExchangeRate exchangeRate;
    @Autowired
    private KLineService kLineService;
    @Autowired
    private ExecutorManager executorManager;


    public void onlineCoinProcessor(CoinProcessorFactory factory, ExchangeCoin coin){
        CoinProcessor nowProcessor =factory.getProcessor(coin.getSymbol());
        if(null == nowProcessor) {
            CoinProcessor processor = new DefaultCoinProcessor(coin.getSymbol(), coin.getBaseSymbol()); //初始化CoinProcessor
            processor.addHandler(mongoMarketHandler);   //存储
            //processor.addHandler(wsHandler);          //Websocket推送
            //processor.addHandler(nettyHandler);       //nettry推送
            processor.addHandler(monitorHandler);       //监控k线、缩略行情、交易明细
            processor.setMarketService(marketService);
            processor.setExchangeRate(exchangeRate); //yangch：循环调用？？
            processor.setKLineService(kLineService);
            processor.setIsHalt(true);

            factory.addProcessor(coin.getSymbol(), processor); //保存交易对和CoinProcessor的关系
            log.info("new processor completed, symbol={} ", coin.getSymbol());

            //初始化执行器，按条件初始化
            executorManager.initExecutor(coin);
            executorManager.initExecutor(processor);
            executorManager.initExecutor(coin, processor);
        } else {
            log.warn("the CoinProcessor already exist, symbol={}", coin.getSymbol());
        }
    }

    public void offlineCoinProcessor(CoinProcessorFactory factory, ExchangeCoin coin){
        factory.removeProcessor(coin.getSymbol());
    }

}
