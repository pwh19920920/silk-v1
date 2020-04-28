package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.mapper.dao.RechargeConfigMapper;
import com.spark.bitrade.entity.RechargeConfig;
import com.spark.bitrade.service.RechargeConfigService;
import org.springframework.stereotype.Service;

/**
 * 充值配置表(RechargeConfig)表服务实现类
 *
 * @author daring5920
 * @since 2019-09-04 10:52:12
 */
@Service("rechargeConfigService")
public class RechargeConfigServiceImpl extends ServiceImpl<RechargeConfigMapper, RechargeConfig> implements RechargeConfigService {

}