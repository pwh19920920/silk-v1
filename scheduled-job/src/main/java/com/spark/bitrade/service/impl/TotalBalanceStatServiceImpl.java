package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.TotalBalanceStat;
import com.spark.bitrade.mapper.dao.TotalBalanceStatMapper;
import com.spark.bitrade.service.ITotalBalanceStatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fumy
 * @time 2018.09.27 19:16
 */
@Service
public class TotalBalanceStatServiceImpl extends ServiceImpl<TotalBalanceStatMapper, TotalBalanceStat> implements ITotalBalanceStatService {


    @Transactional
    @Override
    public int inertNew(TotalBalanceStat totalBalanceStat) {
        return baseMapper.insert(totalBalanceStat);
    }
}
