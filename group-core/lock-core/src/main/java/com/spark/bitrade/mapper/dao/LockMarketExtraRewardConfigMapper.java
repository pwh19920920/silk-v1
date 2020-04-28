package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockMarketExtraRewardConfig;
import com.spark.bitrade.service.SuperMapper;

import java.util.List;

public interface LockMarketExtraRewardConfigMapper extends SuperMapper<LockMarketExtraRewardConfig> {

    /**
     * 查询有效配置
     * @return
     */
    List<LockMarketExtraRewardConfig> findActivityLists();
}
