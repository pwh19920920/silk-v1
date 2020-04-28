package com.spark.bitrade.service.impl;

import com.deaking.risk.RiskEventFactory;
import com.deaking.risk.entity.EventDecide;
import com.deaking.risk.entity.UserInfo;
import com.deaking.risk.entity.result.ResultEntity;
import com.deaking.risk.enums.EnumDecideType;
import com.deaking.risk.enums.act.login.EnumLoginAction;
import com.deaking.risk.event.LoginEvent;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.entity.DeviceIOSInfo;
import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.DevicePCInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.service.ILoginEvent;
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
public class LoginEventImpl implements ILoginEvent {
    @Autowired
    private IRuleCoreService iRuleCoreService;
    @Autowired
    private LocaleMessageSourceService msService;

    @Override
    public MessageResult login(HttpServletRequest request, LoginType type, Member member) {
        try {
            UserInfo user = UserInfo.builder().userId(member.getId()).username(member.getUsername()).build();
            DeviceInfo device = new DeviceInfo("");
            if (type.equals(LoginType.WEB)) {
                //web访问
                device = DevicePCInfo.builder().referer(request.getHeader("Referer")).build();
            }else if (type.equals(LoginType.IOS)){
                //ios访问
//                device = DeviceIOSInfo.builder().build();
            }else {
                //android访问
            }
            LoginEvent event = LoginEvent.builder()
                    .user(user)
                    .device(device)
                    .action(EnumLoginAction.LAUNCH)
                    .operateIp(IpUtils.getIp(request))
                    .build();
            // 发起请求
            ResultEntity<EventDecide> result = iRuleCoreService.execute(RiskEventFactory.createData(event));
            log.info("result => {}", result);
            if (result.isSuccess() && result.getCode() == 200) {
                EventDecide decide = result.getData();
                if (decide.getDecide() == EnumDecideType.BLOCK) {
                    log.info("阻断======");
                    return MessageResult.error(msService.getMessage("LOGIN_BLOCK"));
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
