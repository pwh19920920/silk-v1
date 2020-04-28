package com.spark.bitrade.dto;

import com.spark.bitrade.constant.LockCoinRechargeThresholdType;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.entity.UnlockCoinDetail;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author lingxing
 * @time 2018.07.13 16:14
 */
@Setter
@Getter
public class LockInternalDetailDto {
    private String userName;
    private Long id ;
    List<UnlockCoinDetail> unlockCoinDetailList;
    private long memberId;
    private LockType type;
    private String coinUnit;
    private Long refActivitieId;
    private  BigDecimal totalAmount;
    private  BigDecimal lockPrice;
    private BigDecimal remainAmount;
    private Date lockTime;
    private Date planUnlockTime; //为null，表示解锁时间未知
    private BigDecimal planIncome;
    private Date unlockTime;
    private Date cancleTime;
    private BigDecimal usdtPriceCNY;
    private BigDecimal totalCNY;
    private String remark;
    private String name; //名称
    //交易币种符号
    private String coinSymbol;
    private LockCoinRechargeThresholdType thresholdType; //阀值类型
    private BigDecimal thresholdValue; // 阀值
    private LockCoinRechargeThresholdType unlockType; //释放类型
    private BigDecimal unlockValue; // 释放数量
    private String note; //活动备注
    private LockSettingStatus status; //活动状态
    private Date createTime;//创建时间
    private Date updateTime; //更新时间
    private Long adminId; //操作人员ID
}
