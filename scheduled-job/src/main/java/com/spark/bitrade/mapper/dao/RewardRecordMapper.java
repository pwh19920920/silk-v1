package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.RewardRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-30
 */
@Mapper
public interface RewardRecordMapper extends BaseMapper<RewardRecord> {

    List<RewardRecord> findBBReward(@Param("start") String start, @Param("end") String end);

}
