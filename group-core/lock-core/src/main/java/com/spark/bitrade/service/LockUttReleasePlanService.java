package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.entity.LockUttReleasePlan;

/**
 * <p>
 * UTT释放计划表 服务类
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
public interface LockUttReleasePlanService extends IService<LockUttReleasePlan> {

    void doRelease(LockUttReleasePlan plan, TransactionType type);

}
