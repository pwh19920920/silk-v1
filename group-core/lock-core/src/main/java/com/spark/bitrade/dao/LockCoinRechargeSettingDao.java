package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.dto.LockCoinRechargeSettingDto;
import com.spark.bitrade.entity.LockCoinRechargeSetting;
import org.springframework.stereotype.Repository;

import java.util.List;

/***
 * 锁仓充值配置

 * @author yangch
 * @time 2018.06.12 15:53
 */

@Repository
public interface LockCoinRechargeSettingDao extends BaseDao<LockCoinRechargeSetting> {
    List<LockCoinRechargeSetting> findAllByStatus(LockSettingStatus status);
    //List<LockCoinRechargeSettingDto> queryAllByStatus(LockSettingStatus status);
}
