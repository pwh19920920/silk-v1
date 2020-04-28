package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.C2cFeeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 总c2c交易手续费统计表 Mapper 接口
 * </p>
 *
 * @author fumy
 * @since 2018-06-19
 */
@Mapper
public interface C2cFeeStatMapper extends BaseMapper<C2cFeeStat> {

    List<C2cFeeStat> c2cFeeTotal(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<C2cFeeStat> dayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime);

    String getMaxOpDate();

    int insertAndUpdate(C2cFeeStat cfs);

    int updateTotal(C2cFeeStat cfs);

    //////////////////////////////////“内部商家”C2C交易手续费统计

    List<C2cFeeStat> innerC2cFeeTotal(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("innerMemberId") String innerMemberId);

    List<C2cFeeStat> innerC2cDayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("innerMemberId") String innerMemberId);

    String getInnerMaxOpDate();

    int innerInsertAndUpdate(C2cFeeStat cfs);

    int innerUpdateTotal(C2cFeeStat cfs);

    int innerFeeCount();

    int innerFeeInsert(C2cFeeStat csf);

    //////////////////////////////////“外部商家”C2C交易手续费统计

    List<C2cFeeStat> outerC2cFeeTotal(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("innerMemberId") String innerMemberId);

    List<C2cFeeStat> outerC2cDayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime,@Param("innerMemberId") String innerMemberId);

    String getOuterMaxOpDate();

    int outerInsertAndUpdate(C2cFeeStat cfs);

    int outerUpdateTotal(C2cFeeStat cfs);

    int outerFeeCount();

    int outerFeeInsert(C2cFeeStat csf);


}
