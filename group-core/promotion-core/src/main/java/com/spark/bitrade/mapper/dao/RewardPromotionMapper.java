package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.entity.RewardPromotionSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *用户奖励
 * @author Zhang Yanjun
 * @time 2018.10.10 17:14
*/
@Mapper
public interface RewardPromotionMapper extends BaseMapper<RewardPromotionSetting> {

    /**
     * 根据类型和币种查找配置
     * @author Zhang Yanjun
     * @time 2018.11.26 15:11
     * @param type
     * @param coin
     * @return
     */
    Long findByTypeAndCoin(@Param("type") PromotionRewardType type,@Param("coinId") String coinId);

}
