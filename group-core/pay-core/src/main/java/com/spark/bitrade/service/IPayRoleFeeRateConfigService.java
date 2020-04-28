package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.PayRoleFeeRateConfig;

/**
 * @author Zhang Yanjun
 * @time 2019.01.21 09:14
 */
public interface IPayRoleFeeRateConfigService extends IService<PayRoleFeeRateConfig> {

    PayRoleFeeRateConfig findByIdAndTradeUnit(long id,String tradeUnit);
}
