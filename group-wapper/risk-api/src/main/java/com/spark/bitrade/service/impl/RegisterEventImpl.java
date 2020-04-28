package com.spark.bitrade.service.impl;

import com.deaking.risk.RiskEventFactory;
import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.UserInfo;
import com.deaking.risk.entity.result.ResultEntity;
import com.deaking.risk.enums.EnumDecideType;
import com.deaking.risk.enums.act.login.EnumLoginAction;
import com.deaking.risk.event.LoginEvent;
import com.deaking.risk.event.RegisterEvent;
import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.DevicePCInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.IRegisterEvent;
import com.spark.bitrade.service.LocaleMessageSourceService;
import com.spark.bitrade.service.remote.IRuleCoreService;
import com.spark.bitrade.util.IpUtils;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Zhang Yanjun
 * @time 2018.12.26 15:41
 */
@Component
@Slf4j
public class RegisterEventImpl implements IRegisterEvent {
    @Autowired
    private IRuleCoreService iRuleCoreService;
    @Autowired
    private LocaleMessageSourceService msService;

    @Override
    public MessageResult register(HttpServletRequest request, DeviceInfo device, Member member, String inviteCode) {
        try {
            UserInfo user = UserInfo.builder().mobile(member.getMobilePhone()).email(member.getEmail()).build();
            device = DevicePCInfo.builder().referer(request.getHeader("Referer")).build();
            RegisterEvent event = RegisterEvent.builder()
                    .user(user)
                    .device(device)
                    .operateIp(IpUtils.getIp(request))
                    .inviteCode(inviteCode).build();
            // 发起请求
            ResultEntity<EventDecide> result = iRuleCoreService.execute(RiskEventFactory.createData(event));
            log.info("result => {}", result);
            if (result.isSuccess() && result.getCode() == 200) {
                EventDecide decide = result.getData();
                if (decide.getDecide() == EnumDecideType.BLOCK) {
                    log.info("阻断====");
                    return MessageResult.error(msService.getMessage("REGISTER_BLOCK"));
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
