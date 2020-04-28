package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.PayPaymentCodeManage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 *  付款码管理服务接口
 *
 * @author yangch
 * @time 2019.03.01 14:05
 */
public interface IPayPaymentCodeManageService extends IService<PayPaymentCodeManage> {

    PayPaymentCodeManage findByMemberIdAndAppid(Long memberId, String appId);

    PayPaymentCodeManage save(PayPaymentCodeManage entity);
}
