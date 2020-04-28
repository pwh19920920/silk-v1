package com.spark.bitrade.service;

import com.spark.bitrade.entity.MerchantBuyConfig;
import com.spark.bitrade.mapper.dao.MerchantBuyConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.07.12 10:58  
 */
@Service
public class MerchantBuyConfigService  {

    @Autowired
    private MerchantBuyConfigMapper merchantBuyConfigMapper;

    /**
     * 根据币种获取商家配置
     * @param coinUnit
     * @return
     */
    public List<MerchantBuyConfig> findByCoinUnit(String coinUnit,String appId){
        return merchantBuyConfigMapper.findByCoinUnit(coinUnit,appId);

    }

}
