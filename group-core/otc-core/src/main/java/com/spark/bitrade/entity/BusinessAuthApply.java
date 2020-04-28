package com.spark.bitrade.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商家认证申请信息
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Entity
@Data
@Table
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessAuthApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    /**
     * 认证商家状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus;

    /**
     * 认证失败的原因
     */
    private String detail;
    /**
     * 申请时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditingTime;

    @Column(columnDefinition="TEXT")
    private String authInfo;


    @ManyToOne
    @JoinColumn(name="business_auth_deposit_id")
    private BusinessAuthDeposit businessAuthDeposit;

    private Long lockCoinDetailId;

    /**
     * 保证金数额
     */
    private BigDecimal amount;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;

    @Transient
    private JSONObject info ;

    //>>> add by zyj 2018.12.24 : 添加审核人员信息 start
    /**
     * 审核人员真实姓名
     */
    private String adminRealName;

    /**
     * 审核人员手机号
     */
    private String adminMobilePhone;
    //<<< add by zyj 2018.12.24 : 添加审核人员信息 end
}
