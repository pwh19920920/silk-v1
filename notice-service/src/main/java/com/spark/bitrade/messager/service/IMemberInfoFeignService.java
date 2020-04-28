package com.spark.bitrade.messager.service;

/**
 * @author ww
 * @time 2019.09.19 17:46
 */

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient("UCENTER-API")
public interface IMemberInfoFeignService {

    /**
     * 根据token获取用户信息
     //* @param token
     * @return
     */
    //@Headers({"x-auth-token: #{token}"})
    @RequestMapping(value = "/uc/memberInfo", method = RequestMethod.GET)
    String getUserInfoByToken(@RequestHeader(name = "x-auth-token") String xToken,@RequestHeader(name = "access-auth-token") String accessToken);
    //Object getUserInfoByToken(@RequestParam("token") String token);

    //@Headers({"x-auth-token: #{token}"})
    @RequestMapping(value = "/uc/memberInfo", method = RequestMethod.GET)
    String getUserInfoByCookie(@CookieValue(name = "SESSION") String cookie);
    //Object getUserInfoByToken(@RequestParam("token") String token);
}
