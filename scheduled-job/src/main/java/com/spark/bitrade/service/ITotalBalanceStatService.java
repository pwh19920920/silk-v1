package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.TotalBalanceStat;

/**
 * @author fumy
 * @time 2018.09.27 19:15
 */
public interface ITotalBalanceStatService extends IService<TotalBalanceStat> {

    int inertNew(TotalBalanceStat totalBalanceStat);
}
