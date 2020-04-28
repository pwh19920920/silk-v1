package com.spark.bitrade.service;

import com.spark.bitrade.entity.PayMannerConfig;
import com.spark.bitrade.mapper.dao.PayMannerConfigMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class PayMannerConfigService {

    @Resource
    private PayMannerConfigMapper payMannerConfigMapper;

    public int insert(PayMannerConfig pojo){
        return payMannerConfigMapper.insert(pojo);
    }

    public int insertList(List< PayMannerConfig> pojos){
        return payMannerConfigMapper.insertList(pojos);
    }

    public List<PayMannerConfig> select(PayMannerConfig pojo){
        return payMannerConfigMapper.select(pojo);
    }

    public int update(PayMannerConfig pojo){
        return payMannerConfigMapper.update(pojo);
    }

}
