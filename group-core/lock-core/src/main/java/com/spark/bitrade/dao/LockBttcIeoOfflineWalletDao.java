package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.LockBttcIeoOfflineWallet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * (LockBttcIeoOfflineWallet)表数据库访问层
 *
 * @author dengdy
 * @since 2019-05-08 09:24:51
 */
@Repository
public interface LockBttcIeoOfflineWalletDao extends BaseDao<LockBttcIeoOfflineWallet> {

    LockBttcIeoOfflineWallet findByMemberId(Long memberId);

    /**
     * 减少金钥匙账户金钥匙数量，并更新到相关transaction记录中，以实现事务一致性
     * @param walletId
     * @param am
     * @return
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "update lock_bttc_ieo_offline_wallet set last_release_amount = if(balance < :am, balance , :am),unlocked_amount = unlocked_amount + if(balance < :am, balance , :am),balance = balance - if(balance < :am, balance , :am) where id= :walletId", nativeQuery = true)
    int decreaseOfflineWallet(@Param("walletId") Long walletId, @Param("am") BigDecimal am);


    @Modifying(clearAutomatically = true)
    @Query(value = "update lock_bttc_ieo_offline_wallet set last_release_amount =  :am ,unlocked_amount = unlocked_amount + :am ,balance = balance + :am  where id= :id", nativeQuery = true)
    int addBalance(@Param("id") Long id, @Param("am") BigDecimal am);

}