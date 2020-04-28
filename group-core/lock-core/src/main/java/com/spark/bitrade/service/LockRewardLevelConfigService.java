package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.LockRewardLevelConfigDao;
import com.spark.bitrade.entity.LockRewardLevelConfig;
import com.spark.bitrade.mapper.dao.LockRewardLevelConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fumy
 * @time 2018.12.03 20:38
 */
@Service
public class LockRewardLevelConfigService {

    @Autowired
    private LockRewardLevelConfigDao lockRewardLevelConfigDao;

    @Autowired
    private LockRewardLevelConfigMapper lockRewardLevelConfigMapper;



    /**
     * 保存级差配置管理信息
     * @author fumy
     * @time 2018.12.03 20:39
     * @param levelConfig
     * @return true
     */
    @CacheEvict(cacheNames = "lockRewardLevelConfig", allEntries = true)
    public LockRewardLevelConfig save(LockRewardLevelConfig levelConfig){
        return  lockRewardLevelConfigDao.save(levelConfig);
    }


    /**
     * 查询级差配置信息列表
     * @author fumy
     * @time 2018.12.03 20:49
     * @param
     * @return true
     */
    @Cacheable(cacheNames = "lockRewardLevelConfig", key = "'entity:lockRewardLevelConfig:all-'+#symbol")
    @ReadDataSource
    public List<LockRewardLevelConfig> getLevelConfigList(String symbol){
        return lockRewardLevelConfigMapper.getLockRewardLevelConfigList(symbol);
    }

    /**
     * 根据id查询级差配置信息
     * @author fumy
     * @time 2018.12.03 20:49
     * @param
     * @return true
     */
    @Cacheable(cacheNames = "lockRewardLevelConfig", key = "'entity:lockRewardLevelConfig:'+#levelId+'-'+#symbol")
    @ReadDataSource
    public LockRewardLevelConfig getLevelConfigById(Integer levelId, String symbol){
        return lockRewardLevelConfigMapper.getLevelConfigById(levelId, symbol);
    }

    /**
     * 查询最低级的极差配置信息
     * @author tansitao
     * @time 2018/12/5 14:55 
     */
    @Cacheable(cacheNames = "lockRewardLevelConfig", key = "'entity:lockRewardLevelConfig:minimum-'+#symbol")
    @ReadDataSource
    public LockRewardLevelConfig findMinimum(String symbol){
        return lockRewardLevelConfigMapper.findMinimum(symbol);
    }

}
