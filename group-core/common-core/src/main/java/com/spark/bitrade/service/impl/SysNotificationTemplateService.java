package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.SysNotificationTemplate;
import com.spark.bitrade.mapper.dao.SysNotificationTemplateMapper;
import com.spark.bitrade.service.ISysNotificationTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
  * 通知内容模版实现类
  * @author tansitao
  * @time 2018/7/19 11:16 
  */
@Service
@Slf4j
public class SysNotificationTemplateService extends ServiceImpl<SysNotificationTemplateMapper, SysNotificationTemplate> implements ISysNotificationTemplateService {

    @Autowired
    private SysNotificationTemplateMapper mapper;

    /**
     * 通过消息类型，渠道类型，和语言环境查询内容模板
     * @author tansitao
     * @time 2018/12/19 17:46 
     */
    @Override
    public SysNotificationTemplate findByType(int type, int notificationType, String language) {
        return mapper.findByType(type, notificationType, language);
    }
}
