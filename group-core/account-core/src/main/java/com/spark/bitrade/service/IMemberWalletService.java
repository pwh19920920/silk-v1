package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
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
import com.spark.bitrade.mapper.dao.MemberWalletMapper;
import com.spark.bitrade.service.Base.BaseService;
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


/**
 *
 * @author shenzucai
 * @time 2019.05.09 19:26
 */
public interface IMemberWalletService extends IService<MemberWallet> {


    MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId);

    MessageResult subtractBalance(MemberWallet memberWallet, BigDecimal amount);

    MessageResult increaseBalance(Long walletId, BigDecimal amount);

}
