package com.spark.bitrade.config;

//import com.spark.bitrade.service.ExchangeMemberDiscountRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/***
 * 
 * @author yangch
 * @time 2018.09.23 13:38
 */
@Component
@Slf4j
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {
//    @Autowired
//    private ExchangeMemberDiscountRuleService exchangeMemberDiscountRuleService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======================应用数据初始化开始=====================");
        //缓存 会员优惠规则（优先初始化缓存）
        //exchangeMemberDiscountRuleService.flushCache();
        log.info("======================应用数据初始化完成=====================\r\n\r\n\r\n");
    }
}
