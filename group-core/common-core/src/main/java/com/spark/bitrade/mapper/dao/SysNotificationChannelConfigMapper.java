package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.SysNotificationChannelConfig;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;
//import org.apache.ibatis.annotations.Mapper;

/**
 * 消息推送渠道配置mapper
 * @author tansitao
 * @time 2018/12/18 14:45 
 */
//@Mapper
public interface SysNotificationChannelConfigMapper extends SuperMapper<SysNotificationChannelConfig> {

        //通过主键id查询配置
        @Override
    SysNotificationChannelConfig selectById(Serializable serializable);

    //通过类型查询配置列表
    List<SysNotificationChannelConfig> findByType(@Param("type")int type);
}
