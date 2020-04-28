package com.spark.bitrade.service;

import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.TradeCoinInfo;
import com.spark.bitrade.util.MessageResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 币币交易事件
 * (用户在平台进行币币交易行为事件)
 * @author Zhang Yanjun
 * @time 2018.12.26 15:25
 */
public interface ITradeCoinEvent {
    MessageResult tradeCoin(HttpServletRequest request, LoginType type, Member member, TradeCoinInfo tradeCoinInfo);
}


