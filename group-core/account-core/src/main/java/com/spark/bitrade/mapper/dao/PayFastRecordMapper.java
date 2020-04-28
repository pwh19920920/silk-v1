package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.service.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.spark.bitrade.entity.PayFastRecord;

@Mapper
public interface PayFastRecordMapper extends SuperMapper<PayFastRecord> {

//    int insert(@Param("pojo") PayFastRecord pojo);
//
//    int insertList(@Param("pojos") List<PayFastRecord> pojo);
//
//    List<PayFastRecord> select(@Param("pojo") PayFastRecord pojo);
//
//    int update(@Param("pojo") PayFastRecord pojo);

    PayFastRecord findOneByTradeSn(@Param("tradeSn")String tradeSn);

    List<PayFastRecord> findByMember(@Param("unit")String unit,@Param("memberId")Long memberId,@Param("tradeType")int tradeType,
                                     @Param("transferType")int transferType, @Param("platform")String platform,
                                     @Param("platformTo") String platformTo,@Param("startTime")String startTime, @Param("endTime")String endTime);

    List<PayFastRecord> findlist(@Param("transferType")Integer transferType,@Param("startTime")String startTime,@Param("endTime")String endTime,
                                 @Param("fromId")Long fromId,@Param("toId") Long toId,@Param("fromPhone") String fromPhone,
                                 @Param("toPhone") String toPhone,@Param("fromAppid")String fromAppid,@Param("toAppid")String toAppid);

    List<PayFastRecord> findFastRecord(@Param("unit")String unit,@Param("memberId")Long memberId,@Param("fromAppid")String fromAppid,@Param("toAppid")String toAppid,
                                       @Param("startTime")String startTime,@Param("endTime")String endTime);

}
