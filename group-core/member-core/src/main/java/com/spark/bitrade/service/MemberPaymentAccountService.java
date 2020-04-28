package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.MemberPaymentAccountDao;
import com.spark.bitrade.entity.MemberPaymentAccount;
import com.spark.bitrade.mapper.dao.MemberMabatisMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
  * 用户支付账户service
  * @author tansitao
  * @time 2018/8/13 17:56 
  */
@Service
public class MemberPaymentAccountService extends BaseService {
    @Autowired
    private MemberPaymentAccountDao memberPaymentAccountDao;

    @Autowired
    private MemberMabatisMapper mapper;

    public MemberPaymentAccount findOne(long id){
        return memberPaymentAccountDao.findById(id);
    }

    //add by tansitao 时间： 2018/11/9 原因：添加事务和清除缓存
    @CacheEvict(cacheNames = "member", key = "'entity:memberPaymentAccount:'+#memberPaymentAccount.memberId")
    @Transactional(rollbackFor = Exception.class)
    public MemberPaymentAccount save(MemberPaymentAccount memberPaymentAccount){
        return memberPaymentAccountDao.saveAndFlush(memberPaymentAccount);
    }

    /**
     * 通过用户id查询用户支付账户信息
     * @author tansitao
     * @time 2018/8/14 9:58 
     */
    @ReadDataSource
    @Cacheable(cacheNames = "member", key = "'entity:memberPaymentAccount:'+#id")//add by tansitao 时间： 2018/11/9 原因：添加缓存
    public MemberPaymentAccount findPaymentAccountByMemberId(long id){
        return mapper.findPaymentAccountByMemberId(id);
    }
}
