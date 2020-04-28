package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.SysNotificationChannelConfig;

import java.util.List;

/**
 * 消息推送渠道配置service
 * @author tansitao
 * @time 2018/12/18 14:52 
 */
public interface ISysNotificationChannelConfigService extends IService<SysNotificationChannelConfig> {

    //通过类型查询配置列表
    List<SysNotificationChannelConfig> findByType(int type);
}
