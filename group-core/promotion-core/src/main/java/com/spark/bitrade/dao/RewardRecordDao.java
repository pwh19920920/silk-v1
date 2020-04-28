package com.spark.bitrade.dao;

import com.spark.bitrade.constant.PromotionRewardCycle;
import com.spark.bitrade.constant.PromotionRewardType;
import com.spark.bitrade.constant.RewardRecordStatus;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.RewardRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
public interface RewardRecordDao extends BaseDao<RewardRecord> {
    List<RewardRecord> findAllByMemberAndType(Member member, RewardRecordType type);

    //add by yangch 时间： 2018.04.29 原因：合并
    @Query(value = "select coin_id , sum(amount) from reward_record where member_id = :memberId and type = :type group by coin_id",nativeQuery = true)
    List<Object[]> getAllPromotionReward(@Param("memberId") long memberId ,@Param("type") int type);

    /**
     * 准备发放指定条件的佣金，修改状态未 发放中
     *
     * @author yangch
     * @time 2018.06.01 9:50 
     * @param type 佣金发放类型
     * @param status 返佣状态
     * @param promotionRewardCycle 返佣周期
     * @param startTime 返佣开始范围
     * @param endTime   返佣结束范围
     */
    @Modifying
    @Query(value = "update reward_record set status=1 where type=:type and status=:status and reward_cycle=:cycle and create_time>=:stime and create_time<:etime ",nativeQuery = true)
    int preparePayReward(@Param("type") int type , @Param("status") int status,
                          @Param("cycle") int cycle,
                          @Param("stime") Date startTime, @Param("etime") Date endTime);

    /***
      * 根据指定的条件查询前10000条记录
      *
      * @author yangch
      * @time 2018.06.01 13:44 
     * @param status 返佣状态
     * @param type 佣金发放类型
     * @param cycle 返佣周期
     */
    List<RewardRecord> findFirst10000ByStatusAndTypeAndRewardCycle(RewardRecordStatus status, RewardRecordType type, PromotionRewardCycle cycle);

    //修改佣金发放记录的状态
    @Modifying
    @Query(value = "update reward_record set status=:ns,treated_time=now()  where id=:id and status=:os ",nativeQuery = true)
    int updateStatusByIdAndStatus(@Param("id")long id, @Param("os") int oldStatus, @Param("ns")int newStatus);
}
