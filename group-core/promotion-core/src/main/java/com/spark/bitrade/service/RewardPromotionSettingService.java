package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.dao.RewardPromotionSettingDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.RewardPromotionSetting;
import com.spark.bitrade.mapper.dao.RewardPromotionMapper;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Service
public class RewardPromotionSettingService  extends TopBaseService<RewardPromotionSetting,RewardPromotionSettingDao> {

    @Autowired
    private RewardPromotionMapper rewardPromotionMapper;
    @Autowired
    public void setDao(RewardPromotionSettingDao dao) {
        super.setDao(dao);
    }

    @Cacheable(cacheNames = "rewardPromotionSetting", key = "'entity:rewardPromotionSetting:'+#type.getOrdinal()+'-'+#coin.unit")
    public RewardPromotionSetting findByTypeAndCoin(PromotionRewardType type,Coin coin){
        return dao.findByStatusAndTypeAndCoin(BooleanEnum.IS_TRUE, type,coin);
    }

    @Cacheable(cacheNames = "rewardPromotionSetting", key = "'entity:rewardPromotionSetting:'+#type.getOrdinal()")
    public RewardPromotionSetting findByType(PromotionRewardType type){
        return dao.findFirstByStatusAndType(BooleanEnum.IS_TRUE, type);
//        return dao.findByStatusAndType(BooleanEnum.IS_TRUE, type);   //del by tansitao 时间： 2018/11/27 原因：注释掉改方法使用只查一条数据型的方法
    }

    @CacheEvict(cacheNames = "rewardPromotionSetting", key = "'entity:rewardPromotionSetting:'+#setting.type.getOrdinal()")
    public RewardPromotionSetting save(RewardPromotionSetting setting){
        return dao.save(setting);
    }

    @CacheEvict(cacheNames = "rewardPromotionSetting", allEntries = true)
    public void deletes(long[] ids){
        for(long id : ids){
            delete(id);
        }
    }

    public RewardPromotionSetting findOneByType(PromotionRewardType type){
        return dao.findByType(type);
    }

    /**
     * 根据类型和币种查找配置
     * @author Zhang Yanjun
     * @time 2018.11.26 14:36
     * @param type
     * @param coinId
     * @return
     */
    @ReadDataSource
    public Long isExistPromotionSetting(PromotionRewardType type, String coinId){
        return rewardPromotionMapper.findByTypeAndCoin(type, coinId);
    }


}
