package com.spark.bitrade.service;

import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.TradeCashInfo;
import com.spark.bitrade.util.MessageResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 法币交易事件
 * （用户在平台进行法币交易行为事件）
 * @author Zhang Yanjun
 * @time 2018.12.26 15:25
 */
public interface ITradeCashEvent {
    MessageResult tradeCash(HttpServletRequest request, DeviceInfo device, Member member, TradeCashInfo tradeCashInfo);
}


