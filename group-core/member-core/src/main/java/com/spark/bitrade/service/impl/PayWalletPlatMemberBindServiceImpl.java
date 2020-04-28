package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.service.IPayWalletPlatMemberBindService;
import com.spark.bitrade.entity.PayWalletPlatMemberBind;
import com.spark.bitrade.mapper.dao.PayWalletPlatMemberBindMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayWalletPlatMemberBindServiceImpl extends ServiceImpl<PayWalletPlatMemberBindMapper, PayWalletPlatMemberBind> implements IPayWalletPlatMemberBindService {

    @Autowired
    PayWalletPlatMemberBindMapper payWalletPlatMemberBindMapper;

    @Override
    @ReadDataSource
    public PayWalletPlatMemberBind findByMemberIdAndAppId(long memberId, String appId) {
        return payWalletPlatMemberBindMapper.findByMemberIdAndAppId(memberId,appId);
    }
}
