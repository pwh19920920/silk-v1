package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.Jackpot;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/***
 * 
 * @author yangch
 * @time 2018.09.14 11:13
 */
public interface JackpotDao extends BaseDao<Jackpot> {
    /**
      * 更改更改奖池余额
      * @author yangch
      * @time 2018.09.18 14:17 
     * @param id 奖池id
     * @param jackpotJalance 奖池余额
     */
    @Modifying
    @Query(value = "update pg_jackpot set jackpot_balance= :jackpotJalance  where id= :id ",nativeQuery = true)
    int updateJackpotJalance(@Param("id") long id, @Param("jackpotJalance") BigDecimal jackpotJalance);
}
