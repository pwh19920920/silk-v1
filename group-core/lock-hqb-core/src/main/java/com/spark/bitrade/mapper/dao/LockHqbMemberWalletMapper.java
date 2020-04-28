package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.LockHqbMemberWallet;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LockHqbMemberWalletMapper extends BaseMapper<LockHqbMemberWallet> {

    /**
     * 插入无重复
     * @param lockHqbMemberWallet
     * @return
     */
    int insertNoRepeat(@Param("lockHqbMemberWallet") LockHqbMemberWallet lockHqbMemberWallet);

  LockHqbMemberWallet findByAppIdAndUnitAndMemberId(@Param("appId") String appId, @Param("unit") String unit, @Param("memberId") Long memberId);

    List<LockHqbMemberWallet> findJoin(@Param("memberId") Long memberId, @Param("appId") String appId);

    /**
     * 转移待确认金额到已确认金额中
     *
     * @param amount 金额
     */
    @Update("UPDATE lock_hqb_member_wallet SET lock_amount = lock_amount + #{amount}, plan_in_amount = plan_in_amount - #{amount} WHERE id = #{id} AND plan_in_amount - #{amount} >= 0")
    int confirmInto(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 转移所有待确认金额到已确认金额中
     */
    @Update("UPDATE lock_hqb_member_wallet SET lock_amount = lock_amount + plan_in_amount, plan_in_amount = 0 WHERE id = #{id}")
    int confirmIntoAll(@Param("id") Long id);

    /**
     * 校验账户平衡
     */
    @Select("SELECT IF(accumulate_in_amount - accumulate_out_amount + accumulate_income = plan_in_amount + lock_amount, 1, 0) FROM lock_hqb_member_wallet WHERE id = #{id}")
    int checkBalance(@Param("id") Long id);

    /**
     * 更新账户收益
     */
    @Update("UPDATE lock_hqb_member_wallet SET accumulate_income = accumulate_income + #{amount}, lock_amount = lock_amount + #{amount} WHERE id = #{id}")
    int updateIncome(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 转出修改账户金额
     * @param lockHqbMemberWallet
     * @param outAmount
     * @return
     */
    int updateWalletByDecrease(@Param("lockHqbMemberWallet") LockHqbMemberWallet lockHqbMemberWallet, @Param("outAmount") BigDecimal outAmount);

    /**
     * 转入修改账户金额
     * @param lockHqbMemberWallet
     * @param inAmount
     * @return
     */
    @Update("UPDATE lock_hqb_member_wallet SET plan_in_amount = plan_in_amount + #{inAmount}," +
            "accumulate_in_amount = accumulate_in_amount + #{inAmount} WHERE id = #{lockHqbMemberWallet.id}")
    int updateWalletByIncrease(@Param("lockHqbMemberWallet") LockHqbMemberWallet lockHqbMemberWallet, @Param("inAmount") BigDecimal inAmount);

    // @Select("SELECT * FROM lock_hqb_member_wallet WHERE (lock_amount > 0) ORDER BY id limit ${beg}, ${size}")
    List<LockHqbMemberWallet> selectAsPage(@Param("appId") Long appId, @Param("coinSymbol") String coinSymbol, @Param("beg") int beg, @Param("size") int size);

    @Select("SELECT * FROM lock_hqb_member_wallet WHERE coin_symbol = #{coinSymbol} AND plan_in_amount + lock_amount > 0 ORDER BY id ASC LIMIT ${beg}, ${size}")
    List<LockHqbMemberWallet> selectBatchTransferPage(@Param("coinSymbol") String coinSymbol, @Param("beg") int beg, @Param("size") int size);
}
