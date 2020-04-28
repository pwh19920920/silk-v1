package com.spark.bitrade.messager.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.messager.service.IMemberInfoFeignService;
import com.spark.bitrade.messager.service.IMemberInfoService;
import com.spark.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author ww
 * @time 2019.09.22 10:45
 */
@Service
public class MemberInfoServiceImpl implements IMemberInfoService {

    @Autowired
    IMemberInfoFeignService memberInfoFeignService;

    @Override
    public AuthMember getLoginMemberByToken(String xToken,String accessToken) {
        MessageResult mr = JSON.parseObject(memberInfoFeignService.getUserInfoByToken(xToken,accessToken), MessageResult.class);
        if (!mr.isSuccess()) {
            return null;
        }
        JSONObject data = JSONObject.parseObject(mr.getData().toString(), JSONObject.class);

        Member member = new Member();
        member.setId(Long.valueOf(data.getString("id")));
        AuthMember authMember = AuthMember.toAuthMember(member);

        LoginType[] loginTypes = LoginType.values();
        LoginType loginType = loginTypes[data.getIntValue("loginType")];
        authMember.setLoginType(loginType);

        return authMember;
    }
}
