package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.PayPaymentCodeManage;
import com.spark.bitrade.mapper.dao.IPayPaymentCodeManageMapper;
import com.spark.bitrade.service.IPayPaymentCodeManageService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *  
 *
 * @author yangch
 * @time 2019.03.01 14:08
 */
@Service
public class IPayPaymentCodeManageServiceImpl
        extends ServiceImpl<IPayPaymentCodeManageMapper, PayPaymentCodeManage>
        implements IPayPaymentCodeManageService {

    @Override
    @Cacheable(cacheNames = "paymentCode", key = "'entity:PayPaymentCodeManage:'+#memberId+'-'+#appId")
    public PayPaymentCodeManage findByMemberIdAndAppid(Long memberId, String appId) {
        PayPaymentCodeManage entity = new PayPaymentCodeManage();
        entity.setMemberId(memberId);
        entity.setAppId(appId);

        return baseMapper.selectOne(entity);
    }

    @CacheEvict(cacheNames = "paymentCode", key = "'entity:PayPaymentCodeManage:'+#entity.getMemberId()+'-'+#entity.getAppId()")
    @Override
    public PayPaymentCodeManage save(PayPaymentCodeManage entity) {
        PayPaymentCodeManage oldEntity  =
                findByMemberIdAndAppid(entity.getMemberId(), entity.getAppId());
        if(oldEntity == null){
            baseMapper.insert(entity);
        } else {
            entity.setId(oldEntity.getId());
            baseMapper.updateById(entity);
        }

        return entity;
    }


}
