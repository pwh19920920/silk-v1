package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 投注支入支出记录表
 * @author yangch
 * @time 2018.09.13 10:55
 */

@Entity
@Data
@Table(name = "pg_branch_record")
public class BranchRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    //币种
    @Column(columnDefinition = "varchar(32) comment '币种'")
    private String symbol;

    //数量
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal amount;

    //时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date happenTime;

    //支出会员ID
    private Long expendMemberId;

    //收入会员ID
    private Long incomeMemberId;

    //类型,支出、收入
    @Enumerated(EnumType.ORDINAL)
    private BranchRecordBranchType branchType;

    //业务类型,投注，返佣、回购、开奖奖励、红包奖励、红包投注、开奖短信订阅
    @Enumerated(EnumType.ORDINAL)
    private BranchRecordBusinessType businessType;

    //特殊的无归属的（1：平台账号记录）
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum special;

    //相关联业务的记录ID
    private Long refId;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Long getPeriodId() {
//        return periodId;
//    }
//
//    public void setPeriodId(Long periodId) {
//        this.periodId = periodId;
//    }
//
//    public String getSymbol() {
//        return symbol;
//    }
//
//    public void setSymbol(String symbol) {
//        this.symbol = symbol == null ? null : symbol.trim();
//    }
//
//    public BigDecimal getAmount() {
//        return amount;
//    }
//
//    public void setAmount(BigDecimal amount) {
//        this.amount = amount;
//    }
//
//    public Date getHappenTime() {
//        return happenTime;
//    }
//
//    public void setHappenTime(Date happenTime) {
//        this.happenTime = happenTime;
//    }
//
//    public Long getExpendMemberId() {
//        return expendMemberId;
//    }
//
//    public void setExpendMemberId(Long expendMemberId) {
//        this.expendMemberId = expendMemberId;
//    }
//
//    public Long getIncomeMemberId() {
//        return incomeMemberId;
//    }
//
//    public void setIncomeMemberId(Long incomeMemberId) {
//        this.incomeMemberId = incomeMemberId;
//    }
//
//    public Boolean getBranchType() {
//        return branchType;
//    }
//
//    public void setBranchType(Boolean branchType) {
//        this.branchType = branchType;
//    }
//
//    public Boolean getBusinessType() {
//        return businessType;
//    }
//
//    public void setBusinessType(Boolean businessType) {
//        this.businessType = businessType;
//    }
//
//    public Boolean getSpecial() {
//        return special;
//    }
//
//    public void setSpecial(Boolean special) {
//        this.special = special;
//    }
}