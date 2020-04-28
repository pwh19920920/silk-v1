package com.spark.bitrade.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.mapper.dao.OperationAuditMapper;
import com.spark.bitrade.entity.OperationAudit;
import com.spark.bitrade.service.OperationAuditService;
import org.springframework.stereotype.Service;

/**
 * 运行审核(OperationAudit)表服务实现类
 *
 * @author daring5920
 * @since 2019-09-04 10:51:47
 */
@Service("operationAuditService")
public class OperationAuditServiceImpl extends ServiceImpl<OperationAuditMapper, OperationAudit> implements OperationAuditService {

}