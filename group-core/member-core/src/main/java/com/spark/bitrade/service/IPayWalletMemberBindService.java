package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.PayWalletMemberBind;
import com.spark.bitrade.entity.PayWalletPlatMemberBind;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.01.21 13:59
 */
public interface IPayWalletMemberBindService extends IService<PayWalletMemberBind>{

    int save(PayWalletMemberBind payWalletMemberBind);

    int update(PayWalletMemberBind payWalletMemberBind);

    List<PayWalletMemberBind> findMembers(String walletMarkId,BooleanEnum usable,String appId);

    PayWalletMemberBind findByMemberIdAndAppId(long memberId, String appId);

    PayWalletMemberBind findByMemberIdAndAppIdAndWalletMarkId(long memberId, String appId,String walletMarkId);
}
