package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;

/**
 * 通知内容模版mapper
 * @author tansitao
 * @time 2018/12/18 14:45 
 */
public interface SysNotificationTemplateMapper extends SuperMapper<SysNotificationTemplate> {

    //通过主键id查询内容模版
    @Override
    SysNotificationTemplate selectById(Serializable serializable);

    //通过类型查询内容模版
    SysNotificationTemplate findByType(@Param("type") int type, @Param("notificationType") int notificationType, @Param("language") String language);
}
