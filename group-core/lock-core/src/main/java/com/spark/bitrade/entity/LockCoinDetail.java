package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 锁仓记录
  * @author tansitao
  * @time 2018/6/26 11:09 
  */
@ApiModel
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockCoinDetail {

    @ApiModelProperty(value = "锁仓记录id",name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @ApiModelProperty(value = "会员id",name = "memberId")
    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private long memberId;

    @ApiModelProperty(value = "锁仓类型（0商家保证金、1员工锁仓、2锁仓活动、3理财锁仓、4SLB节点产品、5STO锁仓、6STO增值计划、7IEO锁仓、8金钥匙活动）",name = "type")
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '锁仓类型（0商家保证金、1员工锁仓、2锁仓活动、3理财锁仓、4SLB节点产品、5STO锁仓、6STO增值计划、7IEO锁仓、8金钥匙活动）'")
    private LockType type;

    @ApiModelProperty(value = "活动币种",name = "coinUnit")
    @Column(columnDefinition = "varchar(16) comment '活动币种'")
    private String coinUnit;

    @ApiModelProperty(value = "关联活动ID",name = "refActivitieId")
    @Column(columnDefinition = "bigint(20) comment '关联活动ID'")
    private Long refActivitieId;

    @ApiModelProperty(value = "总锁仓币数",name = "totalAmount")
    @Column(columnDefinition = "decimal(18,8) comment '总锁仓币数'")
    private  BigDecimal totalAmount;

    @ApiModelProperty(value = "锁仓价格相对USDT",name = "lockPrice")
    @Column(columnDefinition = "decimal(18,8) comment '锁仓价格相对USDT'")
    private  BigDecimal lockPrice;

    @ApiModelProperty(value = "剩余锁仓币数",name = "remainAmount")
    @Column(columnDefinition = "decimal(18,8) comment '剩余锁仓币数'")
    private BigDecimal remainAmount;

    @ApiModelProperty(value = "锁仓时间",name = "lockTime")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '锁仓时间'")
    private Date lockTime;


    /**
     * 为null，表示解锁时间未知
     */
    @ApiModelProperty(value = "计划解锁时间",name = "planUnlockTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '计划解锁时间'")
    private Date planUnlockTime;

    @ApiModelProperty(value = "预计收益",name = "planIncome")
    @Column(columnDefinition = "decimal(18,8)  comment '预计收益'")
    private BigDecimal planIncome;

    @ApiModelProperty(value = "状态（0已锁定、1已解锁、2已撤销、3解锁中）",name = "status")
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '状态（已锁定、已解锁、已撤销、解锁中）'")
    private LockStatus status;

    @ApiModelProperty(value = "解锁时间",name = "unlockTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '解锁时间'")
    private Date unlockTime;

    @ApiModelProperty(value = "撤销时间",name = "cancleTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '撤销时间'")
    private Date cancleTime;

    @ApiModelProperty(value = "USDT价格（CNY）",name = "usdtPriceCNY")
    @Column(columnDefinition = "decimal(18,8)  comment 'USDT价格（CNY）'")
    private BigDecimal usdtPriceCNY;

    @ApiModelProperty(value = "锁仓总金额（CNY）",name = "totalCNY")
    @Column(columnDefinition = "decimal(18,8)  comment '锁仓总金额（CNY）'")
    private BigDecimal totalCNY;

    @ApiModelProperty(value = "备注",name = "remark")
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

    /**
     * 锁仓时长（单位：天）
     */
    @ApiModelProperty(value = "锁仓时长（单位：天）",name = "lockDays")
    @Transient
    private Integer lockDays;

    /**
     * 收益保障：最低年化率
     */
    @ApiModelProperty(value = "收益保障：最低年化率",name = "earningRate")
    @Transient
    private BigDecimal earningRate;

    /**
     * 活动每份数量（1表示1个币，大于1表示每份多少币）
     */
    @ApiModelProperty(value = "活动每份数量（1表示1个币，大于1表示每份多少币）",name = "unitPerAmount")
    @Transient
    private BigDecimal unitPerAmount;

    /**
     * 返佣状态，0：默认，不返佣，1：未返佣，2：已返佣
     */
    @ApiModelProperty(value = "返佣状态，0：默认，不返佣，1：未返佣，2：已返佣",name = "lockRewardSatus")
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '状态（不返佣、未返佣、已返佣）'")
    private LockRewardSatus lockRewardSatus;

    @ApiModelProperty(value = "短信发送状态(0:未发送,1:已发送,2:发送失败)",name = "smsSendStatus")
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) comment '短信发送状态(0:未发送,1:已发送,2:发送失败)'")
    private SmsSendStatus smsSendStatus=SmsSendStatus.NO_SMS_SEND;

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
