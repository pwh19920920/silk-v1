package com.spark.bitrade.service.impl;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.PayApplyDao;
import com.spark.bitrade.entity.ThirdPlatformApply;
import com.spark.bitrade.mapper.dao.PayApplyMapper;
import com.spark.bitrade.service.IPayApplyService;
import com.spark.bitrade.vo.PayApplyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fumy
 * @time 2018.10.23 09:37
 */
@Service
public class IPayApplyServiceImpl implements IPayApplyService {

    @Autowired
    PayApplyMapper payApplyMapper;

    @Autowired
    PayApplyDao payApplyDao;

    /**
     * 根据申请key查询第三方合作平台是否存在
     * @author fumy
     * @time 2018.10.23 9:38
     * @param applyKey
     * @return true
     */
    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "thirdPlatform", key = "'entity:thirdPlatform:'+#applyKey")
    public boolean isExistPlatByApplyKey(String applyKey) {
        int count = payApplyMapper.countByApplyKey(applyKey);
        return count > 0 ? true : false;
    }

    /**
     * 保存申请记录
     * @author fumy
     * @time 2018.10.23 10:11
     * @param thirdPlatformApply
     * @return true
     */
    @Transactional
    @Override
    public void save(ThirdPlatformApply thirdPlatformApply) {
        payApplyDao.save(thirdPlatformApply);
    }

    /**
     * 根据商户账号查询申请记录
     * @author fumy
     * @time 2018.10.23 11:27
     * @param busiAccount
     * @return true
     */
    @Override
    @ReadDataSource
    @Cacheable(cacheNames = "thirdPlatformApply", key = "'entity:thirdPlatformApply:'+#busiAccount")
    public PayApplyVo getApplyByAccount(String busiAccount) {
        return payApplyMapper.getApplyByAccount(busiAccount);
    }

    @Override
    @ReadDataSource
    public boolean isExistApply(String busiAccount, String applyKey) {
        int count = payApplyMapper.isExistApply(busiAccount,applyKey);
        return count > 0 ? true : false;
    }
}
