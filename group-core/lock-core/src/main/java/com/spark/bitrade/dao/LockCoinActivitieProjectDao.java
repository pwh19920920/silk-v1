package com.spark.bitrade.dao;

import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockCoinActivitieProject;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


/**
  * 活动
  * @author tansitao
  * @time 2018/6/14 10:27 
  */
@Repository
public interface LockCoinActivitieProjectDao extends BaseDao<LockCoinActivitieProject>
{
    @Modifying
    @Query("update LockCoinActivitieProject lockCoinActivitieProject set lockCoinActivitieProject.boughtAmount = lockCoinActivitieProject.boughtAmount + :boughtAmount where lockCoinActivitieProject.id = :id and (lockCoinActivitieProject.boughtAmount + :boughtAmount) < lockCoinActivitieProject.planAmount")
    int increaseBoughtAmount(@Param("id")long id, @Param("boughtAmount")BigDecimal boughtAmount);

    @Modifying
    @Query("update LockCoinActivitieProject lockCoinActivitieProject set lockCoinActivitieProject.boughtAmount = lockCoinActivitieProject.boughtAmount - :boughtAmount where lockCoinActivitieProject.id = :id ")
    int decreaseBoughtAmount(@Param("id")long id, @Param("boughtAmount")BigDecimal boughtAmount);

    List<LockCoinActivitieProject> findAllByTypeAndStatus(ActivitieType type, LockSettingStatus status);
}
