package com.spark.bitrade.dto;

import com.spark.bitrade.constant.DamagesCalcType;
import com.spark.bitrade.constant.DamagesCoinType;
import com.spark.bitrade.constant.LockCoinActivitieType;
import com.spark.bitrade.constant.LockStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lingxing
 * @time 2018.07.13 16:14
 */
@Getter
@Setter
public class LockDetailDto {
    private String userName;
    private long memberId;
    private String coinUnit;
    private Long refActivitieId;
    private BigDecimal totalAmount;
    private  BigDecimal lockPrice;
    private BigDecimal remainAmount;
    private Date lockTime;
    private Date planUnlockTime; //为null，表示解锁时间未知
    private BigDecimal planIncome;
    private LockStatus status;
    private Date unlockTime;
    private Date cancleTime;
    private BigDecimal usdtPriceCNY;
    private BigDecimal totalCNY;
    private String remark;
    private Long id;
    //关联活动方案ID
    private Long activitieId;
    private String name; //活动名称
    private LockCoinActivitieType type; //活动类型
    //交易币种符号
    private String coinSymbol;
    private BigDecimal unitPerAmount;
    //活动计划数量（币数、份数）
    private BigDecimal planAmount;
    //最低购买数量（币数、份数）
    private BigDecimal minBuyAmount;
    //最大购买数量（币数、份数）
    private BigDecimal maxBuyAmount;
    private Integer lockDays; //锁仓时长（单位：天）
    private Date startTime; //活动开始时间
    private Date endTime; // 活动截止时间
    private Date effectiveTime; //活动生效时间（购买后立即生效，此字段为空）
    private BigDecimal earningRate; //收益保障：最低年化率
    private BigDecimal earningPerUnit; //收益保障：固定返币数量
    private String note; //活动备注
    private Date createTime;//创建时间
    private Date updateTime; //更新时间
    private Long adminId; //操作人员ID
    private DamagesCoinType damagesCoinType;
    private DamagesCalcType damagesCalcType;
    private BigDecimal damagesAmount;
}
