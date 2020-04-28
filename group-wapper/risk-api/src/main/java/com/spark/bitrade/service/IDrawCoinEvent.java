package com.spark.bitrade.service;

import com.spark.bitrade.entity.DeviceInfo;
import com.spark.bitrade.entity.DrawCoinInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.util.MessageResult;

import javax.servlet.http.HttpServletRequest;

/**
 * 提币事件
 * (用户提取虚拟币行为事件)
 * @author Zhang Yanjun
 * @time 2018.12.26 15:25
 */
public interface IDrawCoinEvent {
    MessageResult drawCoin(HttpServletRequest request, DeviceInfo device, Member member, DrawCoinInfo drawCoinInfo);
}


