package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbCoinSettging;
import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.LockHqbCoinSettgingVo;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 16:18
 */
public interface ILockHqbCoinSettgingService extends IService<LockHqbCoinSettging> {

    /**
     * 查询活期宝有效配置列表
     *
     * @param appId 应用或渠道ID
     * @author Zhang Yanjun
     * @time 2019.04.23 18:35
     */
    List<LockHqbCoinSettgingVo> findValidSettingByAppId(String appId);

    /**
     * 无效的活动配置
     *
     * @param appId
     * @author Zhang Yanjun
     * @time 2019.04.26 18:55
     */
    List<LockHqbCoinSettgingVo> findInvalidSettingByAppId(String appId);

    /**
     * 根据应用或渠道ID 和币种查询活期宝有效配置列表
     *
     * @param appId  应用或渠道ID
     * @param symbol 币种
     * @author dengdy
     * @time 2019.04.24 18:35
     */
    LockHqbCoinSettging findValidSettingByAppIdAndSymbol(String appId, String symbol);

    /**
     * 查询用户参与的活期宝币种配置
     *
     * @param appId
     * @param memberId
     * @author Zhang Yanjun
     * @time 2019.04.25 10:57
     */
    List<LockHqbCoinSettgingVo> findJoinSetting(String appId, Long memberId);
}
