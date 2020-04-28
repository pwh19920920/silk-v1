package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BusinessApplyStatus;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ToString
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlockCoinApply {

    public UnlockCoinApply(){}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    @Enumerated(value = EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '申请状态（申请中，通过，未通过）'")
    private BusinessApplyStatus status ;

    @Column(columnDefinition = "bigint(20) comment '关联锁仓记录ID'")
    private long lockCoinDetailId ;

    @Column(columnDefinition = "varchar(255) comment '申请退保原因'")
    private String applyReason ;

    @CreationTimestamp
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '申请时间'")
    private Date applyTime ;

    @Column(columnDefinition = "varchar(255) comment '拒绝退保理由'")
    private String refusalReason;

    @Column(columnDefinition = "varchar(255) comment '取消认证提示'")
    private String reason;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '处理时间'")
    private Date completeTime ;

    //>>> add by zyj 2018.12.24 : 添加审核人员信息 start
    /**
     * 审核人员真实姓名
     */
    @Column(columnDefinition = "varchar(255) comment '审核人员真实姓名'")
    private String adminRealName;

    /**
     * 审核人员手机号
     */
    @Column(columnDefinition = "varchar(255) comment '审核人员手机号'")
    private String adminMobilePhone;
    //<<< add by zyj 2018.12.24 : 添加审核人员信息 end

    @Transient
    private LockCoinDetail lockCoinDetail ;
}
