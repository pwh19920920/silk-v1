package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BettingRecord;
import com.spark.bitrade.entity.BettingState;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
  * 投票记录
  * @author tansitao
  * @time 2018/9/15 10:41 
  */
public interface BettingRecordDao extends BaseDao<BettingRecord> {
    /**
      * 更改中奖状态
      * @author yangch
      * @time 2018.09.18 14:17 
     * @param rangeId 中奖价格区间ID
     */
    @Modifying
    @Query(value = "update pg_betting_record set status=2 where range_id= :rangeId ",nativeQuery = true)
    int updatePraiseStatus(@Param("rangeId") long rangeId);

    /**
      * 未中奖投注状态更改
      * @author yangch
      * @time 2018.09.18 14:17 
     * @param periodId 奖期ID
     * @param rangeId 中奖价格区间ID
     */
    @Modifying
    @Query(value = "update pg_betting_record set status=1 where period_id= :periodId and range_id != :rangeId ",nativeQuery = true)
    int updateLostStatus(@Param("periodId") long periodId, @Param("rangeId") long rangeId);
}
