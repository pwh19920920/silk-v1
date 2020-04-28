package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
  * 锁仓详情
  * @author tansitao
  * @time 2018/8/1 9:10 
  */
@Mapper
public interface LockDetailMapper extends BaseMapper<LockCoinDetail> {

    /**
     * 查询满足解锁条件的锁仓记录
     * @author tansitao
     * @time 2018/8/7 10:46 
     */
    @Select("SELECT * from lock_coin_detail l where l.`status` = #{status} and l.type = #{type} and plan_unlock_time <= #{unlockTime} LIMIT #{lockNum}")
    List<LockCoinDetail> findLockDetailList(@Param("status")int status, @Param("type")int type, @Param("lockNum")int lockNum, @Param("unlockTime")Date unlockTime);

    @Select("SELECT * from unlock_coin_task u where u.`status` = #{status} and type = 1")
    List<UnlockCoinTask> findUnLockTaskList(@Param("status")int status);

    /**
     * 查询满足解锁条件的用户自己锁仓的锁仓记录
     * @author Zhang Yanjun
     * @time 2018.12.11 16:04
     */
    @Select("select * from lock_member_income_plan l where l.reward_time <= #{unlockTime} and l.`status` = #{status} and symbol!='BT'")
    List<LockMemberIncomePlan> findLockMemberList(@Param("status") int status, @Param("unlockTime") Date unlockTime);

    /**
     * 查询满足条件的锁仓记录
     * @author Zhang Yanjun
     * @time 2019.01.02 15:09
     */
    @Select("SELECT * from lock_coin_detail l where l.`status` = #{status} and l.type = #{type} and plan_unlock_time <= #{unlockTime} and ref_activitie_id not in (32,33, 34, 35) ")
    List<LockCoinDetail> findLockDetailByType(@Param("status") int status, @Param("type") int type, @Param("unlockTime") Date unlockTime);

    /**
     * 根据锁仓id查询锁仓详情
     * @param id
     * @return
     */
    LockCoinDetail queryLockDetailById(@Param("id")Long id);

    /**
     * 根据锁仓id查询是否存在返佣记录
     * @param lockDetailId
     * @return
     */
    int isExistRewardRecord(@Param("lockDetailId") Long lockDetailId);

}
