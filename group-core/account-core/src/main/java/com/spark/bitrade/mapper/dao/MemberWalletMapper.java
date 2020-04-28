package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.service.SuperMapper;
import com.spark.bitrade.vo.MemberWalletBalanceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *  * 锁仓详情mapper
 *  * @author tansitao
 *  * @time 2018/6/22 10:54 
 *  
 */
@Mapper
public interface MemberWalletMapper extends SuperMapper<MemberWallet> {


    //注：方法名和要UserMapper.xml中的id一致

    /**
     * 查询指定币种的钱包
     *
     * @param coinName 币种名称，eg：Silubium
     * @param memberId 会员ID
     * @return
     */
    MemberWallet findByCoinAndMemberId(@Param("coinName") String coinName, @Param("memberId") Long memberId);

    /**
     * 查询指定币种的钱包
     *
     * @param coinUnit 币种，eg：SLB
     * @param memberId 会员ID
     * @return
     */
    MemberWallet findByCoinUnitAndMemberId(@Param("coinUnit") String coinUnit, @Param("memberId") Long memberId);

    /**
     *  * 判断地址是否存在
     *  * @author tansitao
     *  * @time 2018/7/31 16:43 
     *  
     */
    String hasExistByAddr(@Param("address") String address);

    /**
     * 会员余额导出
     *
     * @param map
     * @author Zhang Yanjun
     * @time 2018.09.03 14:04
     */
    List<MemberWalletBalanceVO> findAllBy(Map<String, Object> map);

    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Update("update member_wallet wallet set wallet.balance = wallet.balance + #{amount} where wallet.id = #{walletId}")
    int increaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 从钱包余额中减去指定金额
     *
     * @param walletId
     * @param amount
     * @return
     * @author shenzucai
     * @time 2019.05.10 9:50
     */
    @Update("update member_wallet wallet set wallet.balance = wallet.balance - #{amount} where wallet.id = #{walletId} and wallet.balance >= #{amount}")
    int subtractBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 增加锁仓余额 不扣减余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Update("update member_wallet wallet set wallet.lock_balance=#{amount} + wallet.lock_balance where wallet.id=#{walletId}")
    int increaseLockBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    /**
     * 根据memberId 查询有可用余额的钱包币种
     * @param memberId
     * @return
     */
    List<MemberWallet> selectWallerListByMemberId(@Param("memberId") Long memberId);
}
