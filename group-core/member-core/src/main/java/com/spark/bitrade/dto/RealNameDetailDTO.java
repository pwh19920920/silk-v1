package com.spark.bitrade.dto;

import com.spark.bitrade.constant.RealNameStatus;
import lombok.Data;

/**
 * 实名认证详情查询DTO
 *
 * @author zhongxj
 * @time 2019.8.23
 */
@Data
public class RealNameDetailDTO {
    /**
     * 证件类型（0身份证1护照2驾照）
     */
    private String certificateType;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 身份证号码
     */
    private String idNumber;
    /**
     * 国家
     */
    private String country;
    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 实名状态（0-审核失败；1-待认证；2-审核通过）
     */
    private RealNameStatus realNameStatus;
}
