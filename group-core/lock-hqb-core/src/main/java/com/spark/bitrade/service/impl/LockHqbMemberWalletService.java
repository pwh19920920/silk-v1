package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.spark.bitrade.mapper.dao.LockHqbMemberWalletMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class LockHqbMemberWalletService extends ServiceImpl<LockHqbMemberWalletMapper, LockHqbMemberWallet> implements ILockHqbMemberWalletService {

    @Autowired
    private LockHqbMemberWalletMapper lockHqbMemberWalletMapper;

    @Override
    public int save(LockHqbMemberWallet lockHqbMemberWallet) {
        return lockHqbMemberWalletMapper.insert(lockHqbMemberWallet);
    }

    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbMemberWallet:' + #appId + '-' + #unit + '-' + #memberId")
    public synchronized LockHqbMemberWallet findByAppIdAndUnitAndMemberId(String appId, String unit, Long memberId) {
        LockHqbMemberWallet lockHqbMemberWallet = lockHqbMemberWalletMapper.findByAppIdAndUnitAndMemberId(appId, unit, memberId);
        if (lockHqbMemberWallet == null) {
            lockHqbMemberWallet = new LockHqbMemberWallet();
            lockHqbMemberWallet.setMemberId(memberId);
//            lockHqbMemberWallet.setMemberId(74737l);
            lockHqbMemberWallet.setAppId(appId);
            lockHqbMemberWallet.setCoinSymbol(unit);
            lockHqbMemberWallet.setPlanInAmount(BigDecimal.ZERO);
            lockHqbMemberWallet.setLockAmount(BigDecimal.ZERO);
            lockHqbMemberWallet.setAccumulateIncome(BigDecimal.ZERO);
            lockHqbMemberWallet.setAccumulateInAmount(BigDecimal.ZERO);
            lockHqbMemberWallet.setAccumulateOutAmount(BigDecimal.ZERO);
            this.save(lockHqbMemberWallet);
            lockHqbMemberWallet = lockHqbMemberWalletMapper.findByAppIdAndUnitAndMemberId(appId, unit, memberId);
        }
        return lockHqbMemberWallet;
    }

    @Override
    public List<LockHqbMemberWallet> findJoin(Long memberId, String appId) {
        return lockHqbMemberWalletMapper.findJoin(memberId, appId);
    }

    @Override
    public Boolean updateWalletByDecrease(LockHqbMemberWallet lockHqbMemberWallet, BigDecimal outAmount) {
        int updateResult = lockHqbMemberWalletMapper.updateWalletByDecrease(lockHqbMemberWallet, outAmount);
        return updateResult > 0;
    }

    @Override
    public Boolean updateWalletByIncrease(LockHqbMemberWallet lockHqbMemberWallet, BigDecimal outAmount) {
        int updateResult = lockHqbMemberWalletMapper.updateWalletByIncrease(lockHqbMemberWallet, outAmount);
        return updateResult > 0;
    }

    @Override
    @CacheEvict(cacheNames = "lockHqb", key = "'entity:lockHqb:lockHqbMemberWallet:' + #appId + '-' + #unit + '-' + #memberId")
    public void clearWalletCache(Long memberId, String appId, String unit) {
        log.debug("clear wallet cache -> memberId: {}, appId: {}, unit: {}", memberId, appId, unit);
    }
}
