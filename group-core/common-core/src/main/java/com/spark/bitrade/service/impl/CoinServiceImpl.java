package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.mapper.dao.CoinMapper;
import com.spark.bitrade.mapper.dao.SilkDataDistMapper;
import com.spark.bitrade.service.ICoinService;
import com.spark.bitrade.service.ISilkDataDistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper,Coin> implements ICoinService {

    @Autowired
    private CoinMapper coinMapper;

    @Cacheable(cacheNames = "coin", key = "'entity:coinUnit:'+#unit")
    @ReadDataSource
    @Override
    public Coin findByUnit(String unit) {
        return coinMapper.findByUnit(unit);
    }
}
