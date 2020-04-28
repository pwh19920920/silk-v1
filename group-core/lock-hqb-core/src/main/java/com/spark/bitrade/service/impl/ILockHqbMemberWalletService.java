package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.baomidou.mybatisplus.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 16:31
 */
public interface ILockHqbMemberWalletService extends IService<LockHqbMemberWallet> {

    int save(LockHqbMemberWallet lockHqbMemberWallet);

    /**
     * 查询用户某币种账户
     *
     * @param appId
     * @param unit
     * @param memberId
     * @author Zhang Yanjun
     * @time 2019.04.24 13:39
     */
    LockHqbMemberWallet findByAppIdAndUnitAndMemberId(String appId, String unit, Long memberId);


    /**
     * 查询用户参与的币种账户
     *
     * @param
     * @author Zhang Yanjun
     * @time 2019.04.25 13:49
     */
    List<LockHqbMemberWallet> findJoin(Long memberId, String appId);

    /**
     * 转出更新账户余额
     * @author dengdy
     * @param lockHqbMemberWallet
     * @param outAmount
     * @return
     */
    Boolean updateWalletByDecrease(LockHqbMemberWallet lockHqbMemberWallet, BigDecimal outAmount);

    /**
     * 转入更新账户余额
     * @author dengdy
     * @param lockHqbMemberWallet
     * @param inAmount
     * @return
     */
    Boolean updateWalletByIncrease(LockHqbMemberWallet lockHqbMemberWallet, BigDecimal inAmount);

    /**
     * 清除钱包缓存
     * @param memberId
     * @param appId
     * @param unit
     */
    void clearWalletCache(Long memberId, String appId, String unit);

}
