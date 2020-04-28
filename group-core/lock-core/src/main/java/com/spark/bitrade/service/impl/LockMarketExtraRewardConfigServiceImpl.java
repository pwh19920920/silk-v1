package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.LockMarketExtraRewardConfig;
import com.spark.bitrade.mapper.dao.LockMarketExtraRewardConfigMapper;
import com.spark.bitrade.service.LockMarketExtraRewardConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LockMarketExtraRewardConfigServiceImpl extends ServiceImpl<LockMarketExtraRewardConfigMapper, LockMarketExtraRewardConfig> implements LockMarketExtraRewardConfigService {

    @Autowired
    private LockMarketExtraRewardConfigMapper lockMarketExtraRewardConfigMapper;

    @Override
    public List<LockMarketExtraRewardConfig> getActivityLists() {
        return lockMarketExtraRewardConfigMapper.findActivityLists();
    }
}
