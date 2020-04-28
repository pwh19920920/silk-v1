package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockHqbThousandsIncomeRecord;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface LockHqbThousandsIncomeRecordMapper extends BaseMapper<LockHqbThousandsIncomeRecord> {

    LockHqbThousandsIncomeRecord yesterdayThousandsIncome(@Param("appId") String appId, @Param("unit") String unit, @Param("time") Integer time);

    List<LockHqbThousandsIncomeRecord> lastMonthThousandsIncome(@Param("appId") String appId, @Param("unit") String unit, @Param("limit") Integer limit);

    /**
     * 查询某天万分收益总额
     *
     * @param symbol
     * @param dateStr
     * @return
     */
    @Select("select * from lock_hqb_thousands_income_record where coin_symbol = #{symbol} and " +
            "op_time = #{queryDate} and app_id = #{appId}")
    LockHqbThousandsIncomeRecord findTenThousandIncomeOfDay(@Param("symbol") String symbol, @Param("queryDate") String dateStr, @Param("appId") String appId);

}
