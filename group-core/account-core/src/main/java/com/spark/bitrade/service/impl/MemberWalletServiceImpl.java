package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.MemberDepositDao;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.dto.MemberWalletDTO;
import com.spark.bitrade.dto.PayRecordDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.mapper.dao.MemberDepositMapper;
import com.spark.bitrade.mapper.dao.MemberWalletMapper;
import com.spark.bitrade.mapper.dao.PayFastRecordMapper;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.BigDecimalUtils;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.MemberWalletBalanceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MemberWalletServiceImpl extends ServiceImpl<MemberWalletMapper, MemberWallet> implements IMemberWalletService {

    @Autowired
    private MemberWalletMapper memberWalletMapper;

    @Override
    public MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId) {
        return memberWalletMapper.findByCoinUnitAndMemberId(coinUnit,memberId);
    }


    /**
     * 从可用余额中减去指定金额
     *
     * @param memberWallet
     * @param amount
     * @author Zhang Yanjun
     * @time 2018.08.06 10:19
     */
    @Override
    public MessageResult subtractBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletMapper.subtractBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("可用余额减去金额失败");
        }
    }



    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return true
     * @author fumy
     * @time 2018.07.09 9:06
     */
    //add by yangch 时间： 2018.05.11 原因：代码合并
    @Override
    public MessageResult increaseBalance(Long walletId, BigDecimal amount) {
        int ret = memberWalletMapper.increaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("增加钱包余额失败");
        }
    }


}
