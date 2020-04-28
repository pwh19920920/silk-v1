package com.spark.bitrade;

import com.spark.bitrade.consumer.ExecutorManager;
import com.spark.bitrade.consumer.HandleTradeCacheAndConsumer;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.service.ExchangeCoinService;
import com.spark.bitrade.service.ExchangeMemberDiscountRuleService;
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
    private HandleTradeCacheAndConsumer handleTradeCacheAndConsumer;
    @Autowired
    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //缓存 会员优惠规则（优先初始化缓存）
        exchangeMemberDiscountRuleService.flushCache();

        List<ExchangeCoin> coins = coinService.findAllEnabled();
        log.info("======================应用依赖关系初始化开始=====================");
        //初始化执行器
        coins.forEach(coin->  executorManager.initExecutor(coin) );
        log.info("======================应用依赖关系初始化完成=====================");


        log.info("======================应用数据初始化开始=====================");
        //恢复数据
        coins.forEach(coin-> handleTradeCacheAndConsumer.recoverData(coin.getSymbol()) );
        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }
}
