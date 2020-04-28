package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dto.PayAccountVo;
import com.spark.bitrade.entity.PaySupportCoinConfig;
import com.spark.bitrade.mapper.dao.PaySupportCoinConfigMapper;
import com.spark.bitrade.service.IPaySupportCoinConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaySupportCoinConfigServiceImpl extends ServiceImpl<PaySupportCoinConfigMapper, PaySupportCoinConfig> implements IPaySupportCoinConfigService {

    @Autowired
    private PaySupportCoinConfigMapper paySupportCoinConfigMapper;

//    public int insert(PaySupportCoinConfig pojo){
//        return paySupportCoinConfigMapper.insert(pojo);
//    }
//
//    public int insertList(List< PaySupportCoinConfig> pojos){
//        return paySupportCoinConfigMapper.insertList(pojos);
//    }
//
//    public List<PaySupportCoinConfig> select(PaySupportCoinConfig pojo){
//        return paySupportCoinConfigMapper.select(pojo);
//    }

//    public int update(PaySupportCoinConfig pojo){
//        return paySupportCoinConfigMapper.update(pojo);
//    }

    /**
     * 账户列表查询（有效币种）
     * @author Zhang Yanjun
     * @time 2019.01.10 17:46
     * @param memberId
     */
    @ReadDataSource
    @Override
//    @Cacheable(cacheNames = "paySupportCoinConfig", key = "'entity:paySupportCoinConfig:memberId-'+#memberId")
    public List<PayAccountVo> findAccountByValidCoinAndAppIdOrderByRankDesc(Long memberId, String appId){
        return paySupportCoinConfigMapper.findAccountByValidCoinAndAppIdOrderByRankDesc(memberId, appId);
    }

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "paySupportCoinConfig", key = "'entity:paySupportCoinConfig:status-'+#status+'&appId-'+#appId")
    public List<PaySupportCoinConfig> findAllByStatusAndAppIdOrderByRankDesc(BooleanEnum status, String appId) {
        return paySupportCoinConfigMapper.findAllByStatusAndAppIdOrderByRankDesc(status.getOrdinal(), appId);
    }


    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "paySupportCoinConfig", key = "'entity:paySupportCoinConfig:unit-'+#unit+'status'+#status")
    public PaySupportCoinConfig findByStatusAndUnit(String unit,Integer status) {
        return selectOne(new EntityWrapper<PaySupportCoinConfig>()
                .where("unit={0}", unit).and("status={0}", status));
    }

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "paySupportCoinConfig", key = "'entity:paySupportCoinConfig:unit-'+#unit+'status'+#status+'appId-'+#appId")
    public PaySupportCoinConfig findByStatusAndUnitAndAppId(String unit, Integer status, String appId) {
        Map<String,Object> unitMap =new HashMap<>();
        unitMap.put("unit",unit);
        unitMap.put("app_id",appId);
        unitMap.put("status",BooleanEnum.IS_TRUE);
        List<PaySupportCoinConfig> coinConfig=paySupportCoinConfigMapper.selectByMap(unitMap);
        return coinConfig.size() == 0 ? null : coinConfig.get(0);
    }
}
