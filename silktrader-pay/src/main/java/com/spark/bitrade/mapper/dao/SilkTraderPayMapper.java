package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.dto.SilkTraderContractDTO;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.entity.SilkTraderContract;
import com.spark.bitrade.entity.SilkTraderTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author fumy
 * @time 2018.07.05 15:12
 */
@Mapper
public interface SilkTraderPayMapper {


    SilkTraderContractDTO findContractById(@Param("id") long id);

    Map<String,Object> findBalanceByPhone(@Param("phone") String phone,@Param("coinUnit") String coinUnit);

    int insertNewPayOrder(SilkTraderTransaction stt);

    Map<String,Object> exsitsPayOrder(@Param("orderId") String orderId,@Param("detailId")String detailId);

    SilkTraderTransaction findOrderByPayId(@Param("silkOrderNo") String silkOrderNo);

    int insertFeeTransaction(MemberTransaction mt);

    int updatePayOrderStatus(SilkTraderTransaction stt);

    Long getWalletIdByMemberId(@Param("memberId") String memberId,@Param("coinUnit") String coinUnit);
}
