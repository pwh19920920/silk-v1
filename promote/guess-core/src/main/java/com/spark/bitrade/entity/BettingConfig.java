package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 投注配置信息表
 *
 * @author yangch
 * @time 2018.09.13 8:53
 */
@Entity
@Data
@Table(name = "pg_betting_config")
public class BettingConfig {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;


    /**
     * 周期/期数
     */
    private String period;

    /**
     * 活动名称
     */
    private String name;


    /**
     *  投注开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;


    /**
     *  投注结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;


    /**
     *  开奖时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date openTime;


    /**
     *  领奖开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date prizeBeginTime;


    /**
     *  领奖结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date prizeEndTime;


    /**
     * 红包领取开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketBeginTime;


    /**
     *  红包领取结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date redpacketEndTime;


    /**
     *  活动状态
     */
    @Enumerated(EnumType.ORDINAL)
    private BettingConfigStatus status = BettingConfigStatus.STAGE_PREPARE;


    /**
     *  备注
     */
    private String remark;


    /**
     *  创建人
     */
    @Column(columnDefinition = "varchar(32) comment '创建人'")
    private String createBy;


    /**
     *  创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    /**
     *  更新人
     */
    @Column(columnDefinition = "varchar(32) comment '更新人'")
    private String updateBy;


    /**
     * 更新时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    /**
     *  是否删除
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum deleted = BooleanEnum.IS_FALSE;


    /**
     *  中奖价格
     */
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal prizePrice;


    /**
     *  投注币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '投注币种'")
    private String betSymbol;


    /**
     *  起投数量限制
     */
    private BigDecimal lowerLimit;


    /**
     *  竞猜币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '竞猜币种'")
    private String guessSymbol;


    /**
     * 奖励币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '奖励币种'")
    private String prizeSymbol;


    /**
     * 红包支付币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '红包币种'")
    private String redpacketSymbol;


    /**
     * 大红包设定比例
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal redpacketGradeRatio;


    /**
     * 红包币种支付数量
     */
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal redpacketUseNum;


    /**
     * 返佣比例
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal rebateRatio;


    /**
     * 奖励比例
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal prizeRatio;

    //回购比例
    /**
     *
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal backRatio;


    /**
     * 红包比例
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal redpacketRatio;


    /**
     * 下期奖池沉淀比例
     */
    @Column(columnDefinition = "decimal(14,4) ")
    private BigDecimal nextPeriodRatio;

    /**
     * 是否开启红包
     */
    @Column(columnDefinition = "int NOT NULL comment '是否开启红包'")
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum redpacketState;

    /**
     * 红包开启最低币种数量
     */
    @Column(columnDefinition = "decimal(18,8) NOT NULL comment '红包开启最低币种数量'")
    private BigDecimal redpacketOpenLimit;

    /**
     * 生成红包系数比值
     */
    @Column(columnDefinition = "decimal(14,4) NOT NULL comment '生成红包系数比值'")
    private BigDecimal redpacketCoefficientRatio;

    /**
     * 红包奖励币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '红包奖励币种'")
    private String redpacketPrizeSymbol;

    /**
     * 短信币种
     */
    @Column(columnDefinition = "varchar(32) NOT NULL comment '短信币种'")
    private String smsSymbol;

    /**
     * 短信支付币种数量
     */
    @Column(columnDefinition = "decimal(18,8) NOT NULL comment '短信支付币种数量'")
    private BigDecimal smsUseNum;
}