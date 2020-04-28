package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.mapper.dao.RechargeConfigHistoryMapper;
import com.spark.bitrade.entity.RechargeConfigHistory;
import com.spark.bitrade.service.RechargeConfigHistoryService;
import org.springframework.stereotype.Service;

/**
 * 配置操作明细（历史）(RechargeConfigHistory)表服务实现类
 *
 * @author daring5920
 * @since 2019-09-04 10:52:27
 */
@Service("rechargeConfigHistoryService")
public class RechargeConfigHistoryServiceImpl extends ServiceImpl<RechargeConfigHistoryMapper, RechargeConfigHistory> implements RechargeConfigHistoryService {

}