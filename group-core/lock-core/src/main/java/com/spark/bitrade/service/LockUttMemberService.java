package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.dto.LockUttDto;
import com.spark.bitrade.entity.LockUttMember;
import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.vo.LockBttcImportVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 导入用户锁仓表 服务类
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
public interface LockUttMemberService extends IService<LockUttMember> {

    /**
     * utt锁仓
     * @param member
     * @param lockUttDto
     */
    void lockUtt(LockUttMember member, LockUttDto lockUttDto);

    /**
     * 创建流水
     * @param memberId
     * @param amount
     * @param comment
     * @param uttId
     * @param address
     * @return
     */
    MemberTransaction creteTransaction(Long memberId,
                                       BigDecimal amount,
                                       String comment,
                                       Long uttId,
                                       String address,
                                       TransactionType transactionType);

    /**
     * 创建流水
     * @param memberId
     * @param amount
     * @param comment
     * @param uttId
     * @param address
     * @return
     */
    MemberTransaction creteTransactionNew(Long memberId,
                                       BigDecimal amount,
                                       String comment,
                                       Long uttId,
                                       String address,
                                       TransactionType transactionType,String coin);

    List<LockBttcImportVo> findBttcImportList(Long memberId, String tableName);


    List<String> findAllImportTable();

    String tableExist(String tableName);
}
