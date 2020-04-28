package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockBttcOfflineWallet;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.vo.LockBttcOfflineWalletVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface LockBttcOfflineWalletMapper {

    /**
     * 根据用户id查询用户账户数据
     * @param memberId
     * @return
     */
    LockBttcOfflineWalletVo findLockBttcOfflineWalletVoByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据用户id查询用户IEO账户数据
     * @param memberId
     * @return
     */
    BigDecimal findLockBttcIeoOfflineWalletBalanceByMemberId(@Param("memberId")Long memberId);

    /**
     * 减少用户金额
     * @param memberId
     * @return
     */
    @Update("update lock_bttc_offline_wallet set balance = if(balance < #{amount},0,balance - #{amount}) where member_id=#{memberId}")
    int decreaseBalanceById(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);

    /**
     * 减少金钥匙账户金钥匙数量，并更新到相关transaction记录中，以实现事务一致性
     * @param walletId
     * @param am
     * @return
     */
    long decreaseOfflineWallet(@Param("walletId") Long walletId, @Param("am") BigDecimal am);

    /**
     * 根据用户id查询数据
     * @param memberId
     * @return
     */
    @Select("select * from lock_bttc_offline_wallet where member_id=#{memberId} limit 1")
    LockBttcOfflineWallet findByMemberId(@Param("memberId") Long memberId);
}
