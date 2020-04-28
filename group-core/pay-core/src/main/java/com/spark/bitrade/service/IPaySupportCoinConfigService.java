package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dto.PayAccountVo;
import com.spark.bitrade.entity.PaySupportCoinConfig;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.01.21 15:08
 */
public interface IPaySupportCoinConfigService extends IService<PaySupportCoinConfig>{

    List<PayAccountVo> findAccountByValidCoinAndAppIdOrderByRankDesc(Long memberId, String appId);

    List<PaySupportCoinConfig> findAllByStatusAndAppIdOrderByRankDesc(BooleanEnum status, String appId);

    PaySupportCoinConfig findByStatusAndUnit(String unit,Integer status);

    PaySupportCoinConfig findByStatusAndUnitAndAppId(String unit,Integer status, String appId);
}
