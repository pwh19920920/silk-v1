package com.spark.bitrade.messager.service;

/**
 * @author ww
 * @time 2019.09.19 17:46
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.entity.transform.AuthMember;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
@Component

public interface IMemberInfoService {

    public AuthMember getLoginMemberByToken(String xToken,String accessToken);

}
