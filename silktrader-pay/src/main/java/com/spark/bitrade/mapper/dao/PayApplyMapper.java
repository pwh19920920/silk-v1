package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.vo.PayApplyVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author fumy
 * @time 2018.10.23 09:39
 */
@Mapper
public interface PayApplyMapper {

    int countByApplyKey(@Param("applyKey") String applyKey);

    PayApplyVo getApplyByAccount(@Param("busiAccount") String busiAccount);

    int isExistApply(@Param("busiAccount")String busiAccount,@Param("applyKey")String applyKey);
}
