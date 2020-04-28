package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.BettingConfig;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.09.13 13:59
 */
public interface BettingConfigDao extends BaseDao<BettingConfig>{

    @Modifying
    @Query(value = "update pg_betting_config set deleted = 1 where id = :id",nativeQuery = true)
    int deleteById(@Param("id") Long id);

    @Modifying
    @Query(value = "update BettingConfig set status = ?2 where id = ?1")
    int updateConfigStatusById(Long id, BettingConfigStatus status);

    /**
     * 更新红包的状态
     * @param id
     * @param status
     * @return
     */
    @Modifying
    @Query(value = "update BettingConfig set redpacket_state = ?2 where id = ?1")
    int updateConfigRedpacketStateById(Long id, int status);

    /***
     * 更新中奖价格
     * @author yangch
     * @time 2018.09.17 23:13 
     * @param id
     * @param prizePrice
     */
    @Modifying
    @Query(value = "update BettingConfig set prizePrice = ?2 where id = ?1")
    int updatePrizePriceById(Long id, BigDecimal prizePrice);
}
