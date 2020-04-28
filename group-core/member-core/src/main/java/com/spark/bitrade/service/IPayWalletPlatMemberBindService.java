package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.PayWalletPlatMemberBind;

/**
 * @author Zhang Yanjun
 * @time 2019.02.25 11:26
 */
public interface IPayWalletPlatMemberBindService extends IService<PayWalletPlatMemberBind> {

    PayWalletPlatMemberBind findByMemberIdAndAppId(long memberId, String appId);
}
