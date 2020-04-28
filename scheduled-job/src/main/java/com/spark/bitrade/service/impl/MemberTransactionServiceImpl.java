package com.spark.bitrade.service.impl;

import com.spark.bitrade.entity.MemberTransaction;
import com.spark.bitrade.mapper.dao.MemberTransactionMapper;
import com.spark.bitrade.service.IMemberTransactionService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-30
 */
@Service
public class MemberTransactionServiceImpl extends ServiceImpl<MemberTransactionMapper, MemberTransaction> implements IMemberTransactionService {

}
