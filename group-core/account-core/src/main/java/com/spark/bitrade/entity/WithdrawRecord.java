package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.AuditStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.security.auth.message.AuthStatus;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提币申请
 *
 * @author Zhang Jinwei
 * @date 2018年01月29日
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 申请人id
     */
    private Long memberId;
    /**
     * 币种
     */
    @JoinColumn(name = "coin_id", nullable = false)
    @ManyToOne
    private Coin coin;
    /**
     * 申请总数量
     */
    @Column(columnDefinition = "decimal(18,8) comment '申请总数量'")
    private BigDecimal totalAmount;
    /**
     * 手续费
     */
    @Column(columnDefinition = "decimal(18,8) comment '手续费'")
    private BigDecimal fee;
    /**
     * 预计到账数量
     */
    @Column(columnDefinition = "decimal(18,8) comment '预计到账数量'")
    private BigDecimal arrivedAmount;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;

    //add by tansitao 时间： 2018/5/1 原因：添加主币手续费
    @Column(columnDefinition = "decimal(18,8) default 0 comment '主币手续费'")
    private BigDecimal baseCoinFree;

    //add by shenzucai 时间： 2018.05.12 原因：用于处理锁
    private int onDeal = 0; // 0未处理，1处理中，2处理完毕
    /**
     * 提现状态
     */
    @Enumerated(EnumType.ORDINAL)
    private WithdrawStatus status = WithdrawStatus.PROCESSING;
    /**
     * 是否是自动提现
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isAuto;
    /**
     * 审核人
     */
    @JoinColumn(name = "admin_id")
    @ManyToOne
    private Admin admin;
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canAutoWithdraw;

    /**
     * 交易编号
     */
    private String transactionNumber;
    /**
     * 提现地址
     */
    private String address;

    private String remark;
    //add by shenzucai 时间： 2018.04.25 原因：添加失败原因字段 start
    /**
     * 提现失败原因
     */
    private String errorRemark;
    //add by shenzucai 时间： 2018.04.25 原因：添加失败原因字段 end


    //add by  shenzucai 时间： 2018.10.29  原因： 匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 start
    @Column(columnDefinition = "varchar(255) comment '手续费抵扣币种单位（不包括当前币种）'")
    private String feeDiscountCoinUnit;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '抵扣币种对应手续费'")
    private BigDecimal feeDiscountAmount;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end

    /**
     * 备注
     */
    @Column(columnDefinition = "varchar(255) comment '用于备注'")
    private String comment;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end
}
