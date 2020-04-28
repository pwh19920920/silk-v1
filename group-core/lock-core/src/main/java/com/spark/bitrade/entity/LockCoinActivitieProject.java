package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ActivitieType;
import com.spark.bitrade.constant.BooleanEnum;
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
public class LockCoinActivitieProject {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name; //活动方案名称

    private ActivitieType type; //活动类型

    //活动币种符号
    private String coinSymbol;

    //富文本的活动类容
    @Column(columnDefinition="TEXT comment '活动内容（富文本）'")
    private String description;

    @Column(columnDefinition="varchar(256) comment '活动连接地址'")
    private String link;

    //活动每份数量（1表示1个币，大于1表示每份多少币）
    @Column(columnDefinition = "decimal(18,8) comment '每份数量'")
    private BigDecimal unitPerAmount;

    //活动计划数量（币数、份数）
    @Column(columnDefinition = "decimal(18,8) comment '活动计划数量（币数、份数）'")
    private BigDecimal planAmount;

    //最低购买数量（币数、份数）
    @Column(columnDefinition = "decimal(18,8) comment '最低购买数量（币数、份数）'")
    private BigDecimal minBuyAmount;

    //最大购买数量（币数、份数）
    @Column(columnDefinition = "decimal(18,8) comment '最大购买数量（币数、份数）'")
    private BigDecimal maxBuyAmount;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime; //活动开始时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime; // 活动截止时间

    private LockSettingStatus status; //活动状态

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;//创建时间

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime; //更新时间

    @JsonIgnore
    private Long adminId; //操作人员ID

    @Column(columnDefinition = "decimal(18,8) default 0 comment '活动参与数量（币数、份数）'")
    private BigDecimal boughtAmount = BigDecimal.ZERO;

    @Transient
    private BooleanEnum isOverdue;//是否过期

    //add by zyj : pc1.3.1 新增
    @Column(columnDefinition = "varchar(255) comment '图片地址'")
    private String imgUrl;

    //add by zyj 2018.11.27 STO锁仓增值新增 start
    @Column(columnDefinition = "varchar(255) comment '标题图片'")
    private String titleImg;

    @Column(columnDefinition = "varchar(255) comment '收益图片'")
    private String incomeImg;

    @Transient
    private String reward;
    //add by zyj 2018.11.27 STO锁仓增值新增 end
}
