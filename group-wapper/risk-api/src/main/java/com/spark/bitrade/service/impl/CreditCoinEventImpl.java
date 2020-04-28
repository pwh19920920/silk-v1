package com.spark.bitrade.service.impl;

import com.deaking.risk.RiskEventFactory;
import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.UserInfo;
import com.deaking.risk.entity.result.ResultEntity;
import com.deaking.risk.enums.EnumDecideType;
import com.deaking.risk.event.CreditCoinEvent;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.ICreditCoinEvent;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.remote.IRuleCoreService;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 充币事件
 * @author Zhang Yanjun
 * @time 2018.12.26 15:41
 */
@Component
@Slf4j
public class CreditCoinEventImpl implements ICreditCoinEvent {
    @Autowired
    private IRuleCoreService iRuleCoreService;
    @Autowired
    private LocaleMessageSourceService msService;

    @Override
    public MessageResult creditCoin(Member member, CreditCoinInfo creditCoinInfo) {
        try {
            UserInfo user = UserInfo.builder().userId(member.getId()).username(member.getUsername()).build();
            //DeviceInfo device2 = DeviceAndroidInfo.builder().imei("434556332322").mac("23:34:34:45:54").build();
            CreditCoinEvent event = CreditCoinEvent.builder()
                    .user(user)
                    .amount(creditCoinInfo.getAmount())
                    .coin(creditCoinInfo.getCoin())
                    .source(creditCoinInfo.getSource())
                    .target(creditCoinInfo.getTarget()).build();
            // 发起请求
            ResultEntity<EventDecide> result = iRuleCoreService.execute(RiskEventFactory.createData(event));
            log.info("result => {}", result);
            if (result.isSuccess() && result.getCode() == 200) {
                EventDecide decide = result.getData();
                if (decide.getDecide() == EnumDecideType.BLOCK) {
                    log.info("阻断======");
                    return MessageResult.error(msService.getMessage("CREDIT_COIN_BLOCK"));
                } else {
                    log.info("通过");
                    return MessageResult.success();
                }
            } else {
                log.warn("请求返回的状态错误,result={}",result);
            }
        }catch (Exception e){
            log.error("调用风控请求接口失败", e);
        }

        return MessageResult.success();
    }

}
