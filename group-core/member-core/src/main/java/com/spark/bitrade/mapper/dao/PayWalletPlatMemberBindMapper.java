package com.spark.bitrade.mapper.dao;


import com.spark.bitrade.entity.PayWalletPlatMemberBind;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PayWalletPlatMemberBindMapper extends SuperMapper<PayWalletPlatMemberBind> {

    PayWalletPlatMemberBind findByMemberIdAndAppId(@Param("memberId")long memberId, @Param("appId")String appId);

}
