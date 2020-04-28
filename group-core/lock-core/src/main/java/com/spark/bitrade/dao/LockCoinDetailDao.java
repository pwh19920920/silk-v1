package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockRewardSatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.CustomerLockCoinDetail;
import com.spark.bitrade.entity.LockCoinDetail;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
  * 增加锁仓详细
  * @author tansitao
  * @time 2018/6/12 16:53 
  */
public interface LockCoinDetailDao extends BaseDao<LockCoinDetail> {
    public LockCoinDetail findById(long id);

//    public List<LockCoinDetail> findByMemberAndStatus(Member member, DepositStatusEnum status);

    /**
     * 查询商家锁仓记录
     * @author fumy
     * @time 2018.06.21 17:03
     * @param memberId
     * @param type
     * @return true
     */
    @Query(value = "select new com.spark.bitrade.entity.CustomerLockCoinDetail(lc.id,lc.coinUnit,lc.totalAmount,lc.remainAmount,lc.lockTime,lc.unlockTime,lc.status)" +
            " from LockCoinDetail lc where lc.memberId=?1 and lc.type=?2")
    public List<CustomerLockCoinDetail> findByMemberIdAndType(long memberId, LockType type);

    //修改状态
    @Modifying
    @Query("update LockCoinDetail a set a.lockRewardSatus=:rewardSatusNew  where  a.id =:id and a.lockRewardSatus=:rewardSatusOld")
    int updateLockRewardSatus( @Param("id") Long id, @Param("rewardSatusOld") LockRewardSatus lockRewardSatusOld,@Param("rewardSatusNew") LockRewardSatus lockRewardSatusNew);

    /**
     * 修改锁仓状态
     * @author tansitao
     * @time 2018/8/3 11:08 
     */
    @Modifying
    @Query("update LockCoinDetail a set a.status=:nowStatus where a.id=:id and a.status=:oldStatus")
    int updateLockCoinStatus(@Param("id") long id, @Param("nowStatus") LockStatus nowStatus, @Param("oldStatus") LockStatus oldStatus);


    /**
     * 修改锁仓剩余金额
     * @author dengdy
     * @time 2019/5/20 11:08
     * @param id 锁仓id
     * @param amount 新的剩余金额
     * @return 修改结果
     */
    @Modifying
    @Query("update LockCoinDetail a set a.remainAmount=:amount where a.id=:id")
    int updateRemainAmount(@Param("id") long id, @Param("amount") BigDecimal amount);

    /**
     * 修改锁仓plan_unlock_time
     * @author huyu
     * @time 2019/6/20 11:08
     * @param amount
     * @return 修改解仓时间
     */
    @Modifying
    @Query("update LockCoinDetail a set a.remainAmount=:amount,a.planUnlockTime=:date where a.id=:id")
    int updateRemainAmountAndPlanUnlockTime(@Param("id") long id, @Param("amount") BigDecimal amount,@Param("date") Date date);

    @Query(value = "select SUM(l.total_amount) from lock_coin_detail l WHERE l.member_id=:memberId and l.type=:type and l.status=:status",nativeQuery = true)
    BigDecimal findRemainLockAmount(@Param("memberId")long memberId,@Param("type")int type,@Param("status")int status);
    /**
     * 修改剩余锁仓金额和锁仓状态
     * @param amount
     * @param status
     * @param id
     * @return
     */
    @Modifying
    @Query("update LockCoinDetail a set a.remainAmount=:amount,a.status=:status where a.id=:id")
    int updateRemainAmountAndLockStatus(@Param("amount")BigDecimal amount,@Param("status")LockStatus status,@Param("id")Long id);

    /**
     * 通过会员id查询已参加的结束活动
     * @param memberId
     * @return
     */
    @Query(value = "select lcd.*" +
            " from lock_coin_detail lcd where lcd.member_id=:memberId group by lcd.ref_activitie_id",nativeQuery = true)
    List<LockCoinDetail> selectByMemberId(@Param("memberId")Long memberId);
}
