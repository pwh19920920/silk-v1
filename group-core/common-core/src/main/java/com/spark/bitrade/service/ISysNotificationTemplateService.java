package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.SysNotificationTemplate;

/**
 * 通知内容模版service
 * @author tansitao
 * @time 2018/12/18 14:52 
 */
public interface ISysNotificationTemplateService extends IService<SysNotificationTemplate> {

    //通过类型查询配置列表
    SysNotificationTemplate findByType(int type, int notificationType, String language);
}
