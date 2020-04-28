package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BranchRecord;

import java.util.List;

/**
  * 投注支入支出记录
  * @author tansitao
  * @time 2018/9/15 10:41 
  */
public interface BranchRecordDao extends BaseDao<BranchRecord> {
    /***
     * 获取指定周期、用户、类型的流水记录
     * @author yangch
     * @time 2018.09.18 11:29 
     * @param periodId 投注周期
     * @param incomeMemberId 用户id
     * @param businessType 业务类型
     */
    List<BranchRecord> queryAllByPeriodIdAndIncomeMemberIdAndBusinessType(
            long periodId, long incomeMemberId, BranchRecordBusinessType businessType);
}
