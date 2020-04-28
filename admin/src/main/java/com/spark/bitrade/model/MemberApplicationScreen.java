package com.spark.bitrade.model;

import com.spark.bitrade.constant.AuditStatus;
import lombok.Data;

@Data
public class MemberApplicationScreen extends AccountScreen{
    private AuditStatus auditStatus;//审核状态
    private String cardNo ; //身份证号
}
