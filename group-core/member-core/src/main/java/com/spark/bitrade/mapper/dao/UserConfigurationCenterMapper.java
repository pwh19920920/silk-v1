package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.UserConfigurationCenter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户配置中心mapper层
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Mapper
public interface UserConfigurationCenterMapper extends BaseMapper<UserConfigurationCenter> {

    /**
     * 获取当前会员，需要发送的消息通道
     *
     * @param memberId        会员ID
     * @param triggeringEvent 事件
     * @return 当前会员，需要发送的消息通道
     */
    UserConfigurationCenter getUserConfigurationCenterByMemberIdAndEvent(@Param("memberId") Long memberId, @Param("triggeringEvent") Integer triggeringEvent);

}
