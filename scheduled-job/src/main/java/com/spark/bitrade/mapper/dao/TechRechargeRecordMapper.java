package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.TechRechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 技术充（减）币记录 Mapper 接口
 * </p>
 *
 * @author fumy
 * @since 2018-06-20
 */
@Mapper
public interface TechRechargeRecordMapper extends BaseMapper<TechRechargeRecord> {

    List<TechRechargeRecord> techRechargeList(@Param("traderMemberId") String traderMemberId,@Param("innerMemberId") String innerMemberId,
                                              @Param("employeeMemberId") String employeeMemberId,@Param("outerMemberId") String outerMemberId);

    int insertRecord(@Param("list") List<TechRechargeRecord> list);

}
