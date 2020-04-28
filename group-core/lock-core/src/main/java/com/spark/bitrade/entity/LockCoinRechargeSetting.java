package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockCoinRechargeThresholdType;
import com.spark.bitrade.constant.LockSettingStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 锁仓充值配置表
  *
 * @author yangch
 * @time 2018.06.12 14:36
 */

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockCoinRechargeSetting {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name; //名称

    //交易币种符号
    private String coinSymbol;

    private LockCoinRechargeThresholdType thresholdType; //阀值类型
    @Column(columnDefinition = "decimal(18,8) comment '阀值'")
    private BigDecimal thresholdValue; // 阀值

    private LockCoinRechargeThresholdType unlockType; //释放类型
    @Column(columnDefinition = "decimal(18,8) comment '释放数量'")
    private BigDecimal unlockValue; // 释放数量

    @Column(columnDefinition = "varchar(2048) comment '活动备注'")
    private String note; //活动备注

    private LockSettingStatus status; //活动状态

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;//创建时间

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime; //更新时间

    private Long adminId; //操作人员ID

    //上一次的解锁价格
    @Transient
    private BigDecimal prevUnlockPrice = BigDecimal.ZERO;
}
