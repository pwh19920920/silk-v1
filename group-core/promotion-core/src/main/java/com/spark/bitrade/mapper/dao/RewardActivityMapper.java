package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.RewardActivitySetting;
import com.spark.bitrade.vo.RewardActivityResp;
import com.spark.bitrade.vo.RewardActivitySettingVO;
import com.spark.bitrade.vo.RewardActivityVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *用户奖励
 * @author Zhang Yanjun
 * @time 2018.10.10 17:14
*/
@Mapper
public interface RewardActivityMapper extends BaseMapper<RewardActivitySetting> {

    //分页
    List<RewardActivitySettingVO> findRewardActivity();

    List<RewardActivityVo> getRewardList();

    RewardActivityResp getRewardActivityDetail(@Param("id")Long id, @Param("tName")String tName);

}
