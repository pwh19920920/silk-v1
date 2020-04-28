package com.spark.bitrade.service;

import com.spark.bitrade.entity.PayRoleConfig;
import com.spark.bitrade.mapper.dao.PayRoleConfigMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class PayRoleConfigService {

    @Resource
    private PayRoleConfigMapper payRoleConfigMapper;

    public int insert(PayRoleConfig pojo){
        return payRoleConfigMapper.insert(pojo);
    }

    public int insertList(List<PayRoleConfig> pojos){
        return payRoleConfigMapper.insertList(pojos);
    }

    public List<PayRoleConfig> select(PayRoleConfig pojo){
        return payRoleConfigMapper.select(pojo);
    }

    public int update(PayRoleConfig pojo){
        return payRoleConfigMapper.update(pojo);
    }

    public PayRoleConfig findOneById(Long id){
        return payRoleConfigMapper.findOneById(id);
    }

    public PayRoleConfig findDefaultRole(){
        return payRoleConfigMapper.findOneById(1L);
    }

}
