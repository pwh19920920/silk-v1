package com.spark.bitrade.service;

import com.spark.bitrade.entity.CreditCoinInfo;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.util.MessageResult;

/**
 * 充币事件
 * (用户充币行为事件)
 * @author Zhang Yanjun
 * @time 2018.12.26 15:25
 */
public interface ICreditCoinEvent {
    MessageResult creditCoin(Member member, CreditCoinInfo creditCoinInfo);
}


