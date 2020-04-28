package com.spark.bitrade;

import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.consumer.HandleOrderCompletedCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private ExchangeCoinService coinService;

    @Autowired
    private ExecutorManager executorManager;

    @Autowired
    private HandleOrderCompletedCacheAndConsumer handleOrderCompletedCacheAndConsumer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //初始化执行器
        executorManager.initExecutor();

        log.info("======================应用数据初始化开始=====================");

        List<ExchangeCoin> coins = coinService.findAllEnabled();
        coins.forEach(coin->{
            //恢复数据
            handleOrderCompletedCacheAndConsumer.recoverData(coin.getSymbol());
        });

        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }
}
