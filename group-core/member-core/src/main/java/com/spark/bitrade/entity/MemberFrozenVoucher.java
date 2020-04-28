package com.spark.bitrade.entity;

import com.spark.bitrade.constant.CertificateType;
import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data // 会员冻结凭证
public class MemberFrozenVoucher implements Serializable {

    private Long id;
    private String realName; // 真实姓名
    private String idCard;   // 证件号码
    private CertificateType certificateType = CertificateType.IDENTITY_CARD; // 证件类型 0:身份证 1: 护照 2: 驾照
    private Long memberId; // 会员ID
    private CommonStatus state; // 状态 0:正常 1:冻结
    private Long userId;   // 操作用户 ID
    private Date createTime;
    private Date updateTime;
}
