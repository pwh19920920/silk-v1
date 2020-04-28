package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.DamagesCalcType;
import com.spark.bitrade.constant.DamagesCoinType;
import com.spark.bitrade.constant.LockCoinActivitieType;
import com.spark.bitrade.constant.LockSettingStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 锁仓活动配置表
  *
 * @author yangch
 * @time 2018.06.12 14:36
 */

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockCoinActivitieSetting {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 关联活动方案ID
     */
    private Long activitieId;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动类型
     */
    private LockCoinActivitieType type;


    /**
     * 交易币种符号
     */
    private String coinSymbol;

    /**
     * 活动每份数量（1表示1个币，大于1表示每份多少币）
     */
    @Column(columnDefinition = "decimal(18,8) comment '每份数量'")
    private BigDecimal unitPerAmount;



    /**
     * 活动计划数量（币数、份数）
     */
    @Column(columnDefinition = "decimal(18,8) comment '活动计划数量（币数、份数）'")
    private BigDecimal planAmount;

    /**
     * 最低购买数量（币数、份数）
     */
    @Column(columnDefinition = "decimal(18,8) comment '最低购买数量（币数、份数）'")
    private BigDecimal minBuyAmount;

    /**
     * 最大购买数量（币数、份数）
     */
    @Column(columnDefinition = "decimal(18,8) comment '最大购买数量（币数、份数）'")
    private BigDecimal maxBuyAmount;

    /**
     * 活动参与数量（币数、份数）
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '活动参与数量（币数、份数）'")
    private BigDecimal boughtAmount = BigDecimal.ZERO;

    /**
     * 锁仓时长（单位：天）
     */
    private Integer lockDays;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 活动截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 活动生效时间（购买后立即生效，此字段为空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveTime;

    /**
     * 收益保障：最低年化率
     */
    @Column(columnDefinition = "decimal(18,8) comment '收益保障：最低年化率'")
    private BigDecimal earningRate;


    /**
     * 收益保障：固定返币数量
     */
    @Column(columnDefinition = "decimal(18,8) comment '收益保障：固定返币数量'")
    private BigDecimal earningPerUnit;

    /**
     * 活动备注
     */
    @Column(columnDefinition = "varchar(2048) comment '活动备注'")
    private String note;

    /**
     * 活动状态
     */
    private LockSettingStatus status;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 操作人员ID
     */
    @JsonIgnore
    private Long adminId;

    @Column(columnDefinition = "varchar(8) comment '提前解锁：违约金类型（币、人民币）'")
    private DamagesCoinType damagesCoinType;

    @Column(columnDefinition = "varchar(8) comment '提前解锁：违约金计算类型（百分比，固定数量）'")
    private DamagesCalcType damagesCalcType;

    @Column(columnDefinition = "decimal(18,8) comment '提前解锁：违约金数目'")
    private BigDecimal damagesAmount;

    /**
     * 奖励系数
     */
    @Column(columnDefinition = "decimal(10,5)  comment '奖励系数（0~1）'")
    private BigDecimal rewardFactor;

    /**
     * 锁仓期数
     */
    @Column(columnDefinition = "int(11) default 1 comment '锁仓期数'")
    private Integer lockCycle;
    /**
     * 开始释放
     */
    @Column(columnDefinition = "int(11) default 0 comment '开始释放'")
    private Integer beginDays;
    /**
     * 每期天数
     */
    @Column(columnDefinition = "int(11) default 0 comment '每期天数'")
    private Integer cycleDays;
    /**
     * 周期比例
     */
    @Column(columnDefinition = "varchar(2048) comment '周期比例'")
    private String cycleRatio;


}
