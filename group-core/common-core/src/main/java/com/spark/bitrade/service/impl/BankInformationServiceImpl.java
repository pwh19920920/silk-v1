package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.BankInformation;
import com.spark.bitrade.mapper.dao.BankInformationMapper;
import com.spark.bitrade.service.BankInformationService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 收付款银行配置 服务实现类
 * </p>
 *
 * @author qiliao
 * @since 2019-09-23
 */
@Service
public class BankInformationServiceImpl extends ServiceImpl<BankInformationMapper, BankInformation> implements BankInformationService {

}
