package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.TransferSilubium;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * silubium-silktrader交易记录表 Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2019-01-21
 */
public interface TransferSilubiumMapper extends BaseMapper<TransferSilubium> {

    /**
      * 根据应用ID/渠道ID查询流水记录
      * @author yangch
      * @time 2019.03.07 19:16 
     * @param memberId 用户ID
     * @param appId 应用ID
     * @param startTime 开始时间，可选
     * @param endTime  截至时间，可选
     */
    List<TransferSilubium> findRecordByUidAndAppId(@Param("unit")String unit,@Param("memberId")Long memberId, @Param("appId") Long appId,
                                             @Param("startTime")String startTime, @Param("endTime")String endTime);
}
