package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockRewardLevelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fumy
 * @time 2018.12.03 20:40
 */
@Mapper
public interface LockRewardLevelConfigMapper {


    List<LockRewardLevelConfig> getLockRewardLevelConfigList(@Param("symbol") String symbol);

    LockRewardLevelConfig getLevelConfigById(@Param("levelId") Integer levelId, @Param("symbol") String symbol);

    /**
     * 查最低级的极差信息
     * @author tansitao
     * @time 2018/12/5 14:57 
     */
    LockRewardLevelConfig findMinimum(@Param("symbol") String symbol);
}
