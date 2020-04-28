package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.entity.BranchRecord;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

public interface BranchRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(BranchRecord record);

    int insertSelective(BranchRecord record);

    BranchRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(BranchRecord record);

    int updateByPrimaryKey(BranchRecord record);

    BigDecimal findRedpacketDeductAmount(Map<String,Object> params);

    /***
     * 根据周期id、指定的业务类型和支付类型统计总额
     * @author yangch
     * @time 2018.09.19 15:58 
       * @param periodId
     * @param businessType
     * @param branchType
     */
    BigDecimal findBusinessAmount(
            @Param("periodId")long periodId, @Param("businessType") BranchRecordBusinessType businessType,
            @Param("branchType") BranchRecordBranchType branchType);
}