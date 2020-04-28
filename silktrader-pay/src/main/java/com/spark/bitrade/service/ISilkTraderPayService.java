package com.spark.bitrade.service;

import com.spark.bitrade.dto.SilkTraderContractDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderTransaction;

import java.util.Map;

/**
 * @author fumy
 * @time 2018.07.05 15:18
 */
public interface ISilkTraderPayService {

    SilkTraderContractDTO findContractById(long id);

    Map<String,Object> getBalanceByPhone(String phone,String coinUnit);

    boolean insertNewPayOrder(SilkTraderTransaction stt);

    SilkTraderTransaction findOrderByPayId(String silkOrderNo);

    boolean insertFeeTransaction(MemberTransaction mt);

    boolean updatePayOrderStatus(SilkTraderTransaction stt);

    Long getWalletIdByMemberId(String memberId,String coinUnit);

    Map<String,Object> exsitsPayOrder(String orderId,String detailId);
}
