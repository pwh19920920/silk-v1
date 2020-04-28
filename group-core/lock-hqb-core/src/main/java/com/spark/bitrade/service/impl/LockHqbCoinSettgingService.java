package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.LockHqbCoinSettging;
import com.spark.bitrade.entity.LockHqbCoinSettgingVo;
import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.spark.bitrade.mapper.dao.LockHqbCoinSettgingMapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.mapper.dao.LockHqbMemberWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LockHqbCoinSettgingService extends ServiceImpl<LockHqbCoinSettgingMapper, LockHqbCoinSettging> implements ILockHqbCoinSettgingService {

    @Autowired
    private LockHqbCoinSettgingMapper lockHqbCoinSettgingMapper;

    @Autowired
    private ILockHqbMemberWalletService iLockHqbMemberWalletService;


    @Override
    @Cacheable(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbCoinSettgingValidList:appId-'+#appId")
    public List<LockHqbCoinSettgingVo> findValidSettingByAppId(String appId) {
        return lockHqbCoinSettgingMapper.findValidSettingByAppId(appId);
    }

    @Override
    @Cacheable(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbCoinSettgingInvalidList:appId-'+#appId")
    public List<LockHqbCoinSettgingVo> findInvalidSettingByAppId(String appId) {
        return lockHqbCoinSettgingMapper.findInvalidSettingByAppId(appId);
    }

    @Override
    @Cacheable(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbCoinSettging:' + #appId + '-' + #symbol")
    public LockHqbCoinSettging findValidSettingByAppIdAndSymbol(String appId, String symbol) {
        return lockHqbCoinSettgingMapper.findValidSettingByAppIdAndSymbol(appId, symbol);
    }

    @Override
    public List<LockHqbCoinSettgingVo> findJoinSetting(String appId, Long memberId) {
        //查询用户参与的币种
        List<LockHqbMemberWallet> memberWallets = iLockHqbMemberWalletService.findJoin(memberId,appId);
        //查询活动配置
        List<LockHqbCoinSettgingVo> settgings = lockHqbCoinSettgingMapper.findByAppId(appId);
        //处理用户参与的活动
        List<LockHqbCoinSettgingVo> joinSettings = new ArrayList<>();
        for (LockHqbCoinSettgingVo settging : settgings) {
            for (LockHqbMemberWallet wallet : memberWallets) {
                if (wallet.getCoinSymbol().equals(settging.getCoinSymbol())) {
                    joinSettings.add(settging);
                }
            }
        }
        return joinSettings;
    }
}
