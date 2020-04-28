package com.spark.bitrade.dao;

import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.RewardActivitySetting;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardActivitySettingDao extends BaseDao<RewardActivitySetting> {
    RewardActivitySetting findByStatusAndType(BooleanEnum booleanEnum, ActivityRewardType type);

    //add by tansitao 时间： 2018/5/11 原因：添加查找活动list方法
    List<RewardActivitySetting> findAllByStatusAndType(BooleanEnum booleanEnum, ActivityRewardType type);

    RewardActivitySetting findByType(ActivityRewardType type);
}
