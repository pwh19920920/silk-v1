package com.spark.bitrade.service.impl;

import com.spark.bitrade.dto.SilkTraderContractDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.SilkTraderTransaction;
import com.spark.bitrade.mapper.dao.SilkTraderPayMapper;
import com.spark.bitrade.service.ISilkTraderPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author fumy
 * @time 2018.07.05 15:19
 */
@Service
public class ISilkTraderPayServiceImpl implements ISilkTraderPayService {

    @Autowired
    SilkTraderPayMapper silkTraderPayMapper;

    /**
     * 通过商家签约的id查询签约信息
     * @author fumy
     * @time 2018.07.05 15:20
     * @param id
     * @return true
     */
    @Override
    public SilkTraderContractDTO findContractById(long id) {
        return silkTraderPayMapper.findContractById(id);
    }

    /**
     * 根据手机号查询出用户的可用SLB余额
     * @author fumy
     * @time 2018.07.06 18:01
     * @param phone
     * @return true
     */
    @Override
    public Map<String, Object> getBalanceByPhone(String phone,String coinUnit) {
        return silkTraderPayMapper.findBalanceByPhone(phone,coinUnit);
    }

    /**
     * 生成支付订单交易记录
     * @param stt
     * @return
     */
    @Override
    public boolean insertNewPayOrder(SilkTraderTransaction stt) {
        int row = silkTraderPayMapper.insertNewPayOrder(stt);
        return row > 0 ? true : false;
    }

    /**
     * 查询生成的支付订单信息
     * @author fumy
     * @time 2018.07.07 22:30
     * @param payId
     * @return true
     */
    @Override
    public SilkTraderTransaction findOrderByPayId(String silkOrderNo) {
        return silkTraderPayMapper.findOrderByPayId(silkOrderNo);
    }

    /**
     * 插入手续费记录
     * @author fumy
     * @time 2018.07.09 10:06
     * @param mt
     * @return true
     */
    @Override
    public boolean insertFeeTransaction(MemberTransaction mt) {
        int row = silkTraderPayMapper.insertFeeTransaction(mt);
        return row > 0 ? true : false;
    }

    /**
     * 修改支付订单的状态
     * @author fumy
     * @time 2018.07.09 10:06
     * @param stt
     * @return true
     */
    @Override
    public boolean updatePayOrderStatus(SilkTraderTransaction stt) {
        int row = silkTraderPayMapper.updatePayOrderStatus(stt);
        return row > 0 ? true : false;
    }

    /**
     * 根据用户id和币种单位获取用户的钱包id
     * @author fumy
     * @time 2018.07.09 10:05
     * @param memberId
     * @param coinUnit
     * @return true
     */
    @Override
    public Long getWalletIdByMemberId(String memberId, String coinUnit) {
        return silkTraderPayMapper.getWalletIdByMemberId(memberId,coinUnit);
    }

    /**
     * 根据商家自身orderId ,签约细节id查询支付订单状态、是否存在
     * @author fumy
     * @time 2018.07.09 19:51
     * @param orderId
     * @param detailId
     * @return true
     */
    @Override
    public Map<String, Object> exsitsPayOrder(String orderId, String detailId) {
        return silkTraderPayMapper.exsitsPayOrder(orderId,detailId);
    }
}
