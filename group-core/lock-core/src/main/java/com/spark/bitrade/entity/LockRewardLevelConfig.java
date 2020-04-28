package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 级差等级配置管理表
 * @author Zhang Yanjun
 * @time 2018.12.03 16:45
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockRewardLevelConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "varchar(20) comment '级别名称'")
    private String level;

    @Column(columnDefinition = "int(11) comment '级别,值越大级别越大'")
    private int levelId;

    @Column(columnDefinition = "decimal(8,4) comment '奖励率'")
    private BigDecimal rewardRate;

    @Column(columnDefinition = "decimal(18,8) comment '总业绩完成量'")
    private BigDecimal performanceTotal;

    @Column(columnDefinition = "int(11) comment '考核部门数'")
    private int subdivisionCount;

    @Column(columnDefinition = "decimal(18,8) comment '小部门业绩'")
    private BigDecimal subdivisionPerformance;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    private Date updateTime;

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;
}
