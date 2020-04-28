package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.FinancialAudit;

/**
 * 财务审核表(FinancialAudit)表服务接口
 *
 * @author daring5920
 * @since 2019-09-04 10:44:36
 */
public interface FinancialAuditService extends IService<FinancialAudit> {

    void insertOrUpdateNew(FinancialAudit financialAudit);

    void virtualWriteSwitch();
}