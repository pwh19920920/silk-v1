package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.entity.PayRoleFeeRateConfig;
import com.spark.bitrade.mapper.dao.PayRoleFeeRateConfigMapper;
import com.spark.bitrade.service.IPayRoleFeeRateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayRoleFeeRateConfigServiceImpl extends ServiceImpl<PayRoleFeeRateConfigMapper, PayRoleFeeRateConfig> implements IPayRoleFeeRateConfigService {

    @Autowired
    private PayRoleFeeRateConfigMapper payRoleFeeRateConfigMapper;

    @Override
    @ReadDataSource
    public PayRoleFeeRateConfig findByIdAndTradeUnit(long id, String tradeUnit) {
        Map<String, Object> feeMap = new HashMap<>();
        feeMap.put("role_id", id);
        feeMap.put("trade_unit", tradeUnit);
        List<PayRoleFeeRateConfig> feeRateConfig = payRoleFeeRateConfigMapper.selectByMap(feeMap);
        return feeRateConfig.size() == 0 ? null : feeRateConfig.get(0);
    }

//    public int insert(PayRoleFeeRateConfig pojo){
//        return payRoleFeeRateConfigMapper.insert(pojo);
//    }
//
//    public int insertList(List< PayRoleFeeRateConfig> pojos){
//        return payRoleFeeRateConfigMapper.insertList(pojos);
//    }
//
//    public PayRoleFeeRateConfig select(PayRoleFeeRateConfig pojo){
//        return payRoleFeeRateConfigMapper.select(pojo);
//    }
//
//    public int update(PayRoleFeeRateConfig pojo){
//        return payRoleFeeRateConfigMapper.update(pojo);
//    }

}
