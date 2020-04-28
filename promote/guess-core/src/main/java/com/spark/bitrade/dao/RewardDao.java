package com.spark.bitrade.dao;

import com.spark.bitrade.constant.RewardStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BranchRecord;
import com.spark.bitrade.entity.Reward;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
  * 中奖记录
  * @author tansitao
  * @time 2018/9/15 10:41 
  */
public interface RewardDao extends BaseDao<Reward> {

    // 批量生成用户中奖记录的参考SQL：
    //insert into pg_reward(period_id,reward_num, symbol, business_type, member_id, promotion_code, status,version,happen_time)
    //select 周期id,bet_num*中奖的兑换汇率,'领奖币种','竞猜类型',member_id, promotion_code,'待领取',0,now()   from pg_betting_record where range_id=中奖区域ID
    /***
     * 批量生成用户的中奖记录
     * @author yangch
     * @time 2018.09.17 22:34 
     * @param periodId  投注ID
     * @param rangeId 中奖价格区间
     * @param cashRatio 兑换汇率
     * @param cashSymbol 兑换币种
     */
    @Modifying
    @Query(value = "insert into pg_reward(period_id,reward_num, symbol, business_type, member_id, promotion_code, status, version, ref_id) "
            +" select period_id, bet_num * :cashRatio , :cashSymbol , 0,member_id, promotion_code, 0,0, id "
            +"from pg_betting_record  where period_id=:periodId and range_id= :rangeId ",nativeQuery = true)
    int batchSavePrizeRecord (@Param("periodId") long periodId, @Param("rangeId") long rangeId,
                                   @Param("cashRatio") BigDecimal cashRatio, @Param("cashSymbol") String cashSymbol);

    /**
     * 弃奖记录处理
     * @author yangch
     * @time 2018.09.18 14:17 
     * @param periodId
     */
    @Modifying
    @Query(value = "update pg_reward set status=2, version =version + 1 where status=0 and business_type=0 and  period_id= :periodId ",nativeQuery = true)
    int autoAbandonPrize(@Param("periodId") long periodId);

    /**
      * 修改奖励状态
      * @author tansitao
      * @time 2018/9/17 21:58 
      */
    @Modifying
    @Query(value = "update pg_reward r set r.status = :status, r.version = r.version + 1, r.get_time = now() where r.id = :id and r.version = :version",nativeQuery = true)
    int updateRewardStatus(@Param("id") long id, @Param("status") int status, @Param("version") int version);
}
