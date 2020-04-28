package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.ExchangeFeeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fumy
 * @since 2018-06-16
 */
@Mapper
public interface ExchangeFeeStatMapper extends BaseMapper<ExchangeFeeStat> {

    List<ExchangeFeeStat> exchangeFeeTotal(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<ExchangeFeeStat> dayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime);

    String getMaxOpDate();

    int insertAndUpdate(ExchangeFeeStat efs);

    int updateTotal(ExchangeFeeStat efs);

    ///////////////////////////////////////////////////操盘账户币币交易手续费统计

    List<ExchangeFeeStat> traderExchangeFeeTotal(@Param("startTime") String startTime,@Param("endTime") String endTime,
                                                 @Param("traderMemberId") String traderMemberId);

    List<ExchangeFeeStat> traderDayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime,
                                             @Param("traderMemberId") String traderMemberId);

    String getTraderMaxOpDate();

    int traderInsertAndUpdate(ExchangeFeeStat efs);

    int traderUpdateTotal(ExchangeFeeStat efs);

    int traderCount();

    int traderInsert(ExchangeFeeStat efs);
}
