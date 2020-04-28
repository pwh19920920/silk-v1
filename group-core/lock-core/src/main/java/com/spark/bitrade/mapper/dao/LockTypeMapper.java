package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.dto.LockAbstractDto;
import com.spark.bitrade.dto.LockCoinDetailDto;
import com.spark.bitrade.dto.LockInternalDetailDto;
import com.spark.bitrade.dto.LockTypeDto;
import com.spark.bitrade.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by lingxing on 2018/7/12.
 */
@Mapper
public interface LockTypeMapper {
    //查询锁仓明细查询
    List<LockTypeDto> findAllLock(@Param("lockAbstract")LockAbstractDto lockAbstract);

    LockCoinDetailDto findByLockDetail(@Param("lockCoinDetailId")Long lockCoinDetailId);

    LockInternalDetailDto findByLockInternalDetail(@Param("lockCoinDetailId")Long lockCoinDetailId);
    List<UnlockCoinDetail>findByLockCoinDetailId(@Param("lockCoinDetailId")Long LockCoinDetailId);
}
