package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.service.ISilkDataDistService;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.mapper.dao.SilkDataDistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SilkDataDistServiceImpl extends ServiceImpl<SilkDataDistMapper, SilkDataDist> implements ISilkDataDistService {

    @Autowired
    private SilkDataDistMapper silkDataDistMapper;

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "silkDataDist", key = "'entity:silkDataDist:v1:dictId-'+#id+'&dictKey-'+#key")
    public SilkDataDist findByIdAndKey(String id, String key) {
        return silkDataDistMapper.findByIdAndKey(id,key);
    }

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "silkDataDistList", key = "'entity:silkDataDistList:dictId-'+#id+'&dictKey-'+#key")
    public List<SilkDataDist> findListByIdAndKey(String id, String key) {
        return silkDataDistMapper.findListByIdAndKey(id, key);
    }

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "silkDataDist", key = "'entity:silkDataDist:v1:dictKey-'+#key")
    public SilkDataDist findByKey(String key) {
        return silkDataDistMapper.findByKey(key);
    }

    @Override
    public Boolean toBoolean(SilkDataDist silkData){
        if(silkData == null){
            return false;
        }
        if("true".equalsIgnoreCase(silkData.getDictVal())
                || "1".equals(silkData.getDictVal())){
            return true;
        }

        return false;
    }
}
