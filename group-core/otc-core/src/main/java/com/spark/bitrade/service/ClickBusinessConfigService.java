package com.spark.bitrade.service;


import com.spark.bitrade.entity.*;
import com.spark.bitrade.mapper.dao.ClickBusinessConfigMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 一键商家买卖币配置
 *
 * @author tansitao
 * @time 2019.01.03 17:22
 */
@Service
public class ClickBusinessConfigService extends BaseService {

    @Autowired
    private ClickBusinessConfigMapper clickBusinessConfigMapper;

    /**
     *  * 获取所有一键买币卖币商家
     *  * @author tansitao
     *  * @time 2019/1/7 14:40 
     *  
     */
    @Cacheable(cacheNames = "clickBusinessConfig", key = "'entity:clickBusinessConfig:all'")
    public List<ClickBusinessConfig> getAllClickBusiness() {
        return clickBusinessConfigMapper.findAll();
    }

    @Cacheable(cacheNames = "clickBusinessConfig", key = "'entity:clickBusinessConfig:' + #coin + '_' + #appId")
    public List<ClickBusinessConfig> getAllClickBusiness(String coin, String appId) {
        return clickBusinessConfigMapper.findByCoinAndAppId(coin, appId);
    }

    public ClickBusinessConfigService getService() {
        return SpringContextUtil.getBean(ClickBusinessConfigService.class);
    }
}
