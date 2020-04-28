package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.mapper.dao.FinancialAuditMapper;
import com.spark.bitrade.entity.FinancialAudit;
import com.spark.bitrade.service.FinancialAuditService;
import org.springframework.stereotype.Service;

/**
 * 财务审核表(FinancialAudit)表服务实现类
 *
 * @author daring5920
 * @since 2019-09-04 10:44:36
 */
@Service("financialAuditService")
public class FinancialAuditServiceImpl extends ServiceImpl<FinancialAuditMapper, FinancialAudit> implements FinancialAuditService {

    @Override
    @WriteDataSource
    public void insertOrUpdateNew(FinancialAudit financialAudit) {
        insertOrUpdate(financialAudit);
    }

    @Override
    @WriteDataSource
    public void virtualWriteSwitch() {
    }
}