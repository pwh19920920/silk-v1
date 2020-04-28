package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BettingState;

/***
 * 
 * @author yangch
 * @time 2018.09.14 11:13
 */
public interface BettingStateDao extends BaseDao<BettingState> {
    BettingState findBettingStateByPeriodIdAndOperate(Long periodId, BettingStateOperateType operate);
}
