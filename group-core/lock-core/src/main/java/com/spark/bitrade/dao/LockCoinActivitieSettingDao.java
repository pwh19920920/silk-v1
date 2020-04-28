package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockCoinActivitieType;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/***
 * 锁仓活动配置

 * @author yangch
 * @time 2018.06.12 16:41
 */

@Repository
public interface LockCoinActivitieSettingDao extends BaseDao<LockCoinActivitieSetting> {

    /**
     * 通过activitieId获取所有子活动配置
     * @author tansitao
     * @time 2018/6/14 14:02 
     */
    List<LockCoinActivitieSetting> findByActivitieIdAndStatusOrderByLockDays(long activitieId, LockSettingStatus status);


    List<LockCoinActivitieSetting> findLockCoinActivitieSettingsByTypeAndStatusEquals(LockCoinActivitieType type,LockSettingStatus status);

    /**
     * 查询SLB节点产品
     * @author fumy
     * @time 2018.08.07 9:40
     * @param
     * @return true
     */
    @Query(value = "SELECT * FROM `lock_coin_activitie_setting` ls LEFT JOIN lock_coin_activitie_project lc ON lc.id = ls.activitie_id where ls.type = 1 and ls.`status` = 1 and lc.type = 3",nativeQuery = true)
    List<LockCoinActivitieSetting> findLockCoinActivitieSettingsByTypeAndStatusAndActivitieId();

    @Modifying
    @Query(value = "update lock_coin_activitie_setting set bought_amount = bought_amount + ?1  where id = ?2", nativeQuery = true)
    int updateLockCoinActivitieSettingsById(BigDecimal amount, Long id);
}
