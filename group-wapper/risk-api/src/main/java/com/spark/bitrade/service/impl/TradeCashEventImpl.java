package com.spark.bitrade.service.impl;

import com.deaking.risk.RiskEventFactory;
import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.UserInfo;
import com.deaking.risk.entity.result.ResultEntity;
import com.deaking.risk.enums.EnumDecideType;
import com.deaking.risk.enums.EnumScene;
import com.deaking.risk.event.TradeCashEvent;
import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.TradeCashInfo;
import com.spark.bitrade.service.ITradeCashEvent;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.remote.IRuleCoreService;
import com.spark.bitrade.util.IpUtils;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 法币交易事件
 * @author Zhang Yanjun
 * @time 2018.12.27 14:17
 */
@Component
@Slf4j
public class TradeCashEventImpl implements ITradeCashEvent{
    @Autowired
    private IRuleCoreService iRuleCoreService;
    @Autowired
    private LocaleMessageSourceService msService;

    @Override
    public MessageResult tradeCash(HttpServletRequest request, DeviceInfo device, Member member, TradeCashInfo tradeCashInfo) {
        try {
            UserInfo user = UserInfo.builder().userId(member.getId()).username(member.getUsername()).build();
            UserInfo targetUser = UserInfo.builder().userId(tradeCashInfo.getTargetUser().getId()).username(tradeCashInfo.getTargetUser().getUsername()).build();
            //DeviceInfo device2 = DeviceAndroidInfo.builder().imei("434556332322").mac("23:34:34:45:54").build();
            TradeCashEvent event = TradeCashEvent.builder()
                    .user(user)
                    .device(device)
                    .action(EnumScene.TRADE_CASH.ordinal())
                    .direction(tradeCashInfo.getDirection())
                    .amount(tradeCashInfo.getAmount())
                    .targetUser(targetUser)
                    .coin(tradeCashInfo.getCoin())
                    .operateIp(IpUtils.getIp(request)).build();
            // 发起请求
            ResultEntity<EventDecide> result = iRuleCoreService.execute(RiskEventFactory.createData(event));
            log.info("result => {}", result);
            if (result.isSuccess() && result.getCode() == 200) {
                EventDecide decide = result.getData();
                if (decide.getDecide() == EnumDecideType.BLOCK) {
                    log.info("阻断======");
                    return MessageResult.error(msService.getMessage("C2C_BLOCK"));
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
