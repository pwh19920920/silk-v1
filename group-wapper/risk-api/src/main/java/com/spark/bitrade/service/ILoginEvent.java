package com.spark.bitrade.service;

import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.util.MessageResult;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Zhang Yanjun
 * @time 2018.12.26 15:25
 */
public interface ILoginEvent {
    MessageResult login(HttpServletRequest request , LoginType type, Member member);
}


