package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.MemberWallet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MemberWalletDao extends BaseDao<MemberWallet> {

    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount where wallet.id = :walletId")
    int increaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 增加钱包锁仓余额
     * 2018-06-12 18:48:43 by yangch
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.lockBalance = wallet.lockBalance + :amount where wallet.id = :walletId")
    int increaseLockBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 减少钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int decreaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 冻结钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount,wallet.frozenBalance=wallet.frozenBalance + :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int freezeBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 冻结钱包余额到锁仓余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount,wallet.lockBalance=wallet.lockBalance + :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int freezeBalanceToLockBlance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 解冻钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount,wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int thawBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 从锁仓余额解冻钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount,wallet.lockBalance=wallet.lockBalance - :amount where wallet.id = :walletId and wallet.lockBalance >= :amount")
    int thawBalanceFromLockBlance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /*
    * 从钱包余额中减去指定金额
    * @author Zhang Yanjun
    * @time 2018.08.06 11:23
     * @param walletId
     * @param amount
    */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance- :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int subtractBalance (@Param("walletId")long walletId,@Param("amount")BigDecimal amount);

    /*
    * 从钱包冻结余额中减去指定金额
    * @author Zhang Yanjun
    * @time 2018.08.06 11:25
     * @param walletId
     * @param amount
    */
    @Modifying
    @Query("update MemberWallet wallet set wallet.frozenBalance=wallet.frozenBalance- :amount where wallet.id= :walletId and wallet.frozenBalance >= :amount")
    int subtractFreezeBalance (@Param("walletId")long walletId,@Param("amount")BigDecimal amount);

    /*
    *从锁仓余额中减去指定金额
    * @author Zhang Yanjun
    * @time 2018.08.06 13:52
     * @param walletId
     * @param amount
    * @return  * @param walletId
     * @param amount
    */
    @Modifying
    @Query("update MemberWallet wallet set wallet.lockBalance=wallet.lockBalance- :amount where wallet.id= :walletId and wallet.lockBalance >= :amount")
    int subtractLockBalance(@Param("walletId")long walletId,@Param("amount")BigDecimal amount);

    /**
     * 更新钱包和冻钱包余额
     *
     * @param walletId
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :balanceAmount,wallet.lockBalance = wallet.lockBalance - :lockBalanceAamount where wallet.id = :walletId and wallet.lockBalance >= :lockBalanceAamount")
    int updateBlanceAndLockBlance(@Param("walletId") long walletId, @Param("balanceAmount") BigDecimal addBalanceAmount, @Param("lockBalanceAamount") BigDecimal subLockBalanceAamount);

    /**
     * 减少冻结余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int decreaseFrozen(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 减少锁仓余额
     * @author tansitao
     * @time 2018/7/4 10:03 
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.lockBalance=wallet.lockBalance - :amount where wallet.id = :walletId and wallet.lockBalance >= :amount")
    int decreaseLockFrozen(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    MemberWallet findByCoinAndAddress(Coin coin, String address);

    MemberWallet findByCoinAndMemberId(Coin coin, Long memberId);

    List<MemberWallet> findAllByMemberId(Long memberId);

    @Modifying
    @Query("update MemberWallet wallet set wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int decreaseFreezeBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    List<MemberWallet> findAllByCoin(Coin coin);

    @Query(value="select sum(a.balance)+sum(a.frozen_balance) as allBalance from member_wallet a where a.coin_id = :coinName",nativeQuery = true)
    BigDecimal getWalletAllBalance(@Param("coinName")String coinName);


    /**
     * 修改钱包地址
     * @author shenzucai
     * @time 2018.11.20 9:33
     * @param walletId
     * @param address
     * @return true
     */
    @Modifying
    @Query(value="update member_wallet set address = :address where id = :walletId",nativeQuery = true)
    int updateMemberWalletAddress(@Param("walletId") long walletId, @Param("address") String address);

    /**
     * @author lingxing
     * 手动平账 修改 当前余额+佣金
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount where wallet.id = :walletId")
    int commissionBalanceFromBlance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);
}
