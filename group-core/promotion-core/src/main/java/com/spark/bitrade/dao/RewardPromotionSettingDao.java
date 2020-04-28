package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.RewardPromotionSetting;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardPromotionSettingDao extends BaseDao<RewardPromotionSetting> {

    RewardPromotionSetting findByStatusAndTypeAndCoin(BooleanEnum booleanEnum, PromotionRewardType type, Coin coin);

    RewardPromotionSetting findByStatusAndType(BooleanEnum booleanEnum, PromotionRewardType type);

    /**
     * 通过状态和类型查询一条佣金参数配置
     * @author tansitao
     * @time 2018/11/27 9:52 
     */
    RewardPromotionSetting findFirstByStatusAndType(BooleanEnum booleanEnum, PromotionRewardType type);

    RewardPromotionSetting findByType(PromotionRewardType type);
}
