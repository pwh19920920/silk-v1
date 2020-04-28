package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员CNYT市场等级表
 * @author Zhang Yanjun
 * @time 2018.12.03 18:28
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockMarketLevel {
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private String id;

    private Long memberId;

    /**
     * lock_reward_level_config.member_level_id
     */
    @Column(columnDefinition = "bigint(20) comment '会员等级id'")
    private Long memberLevelId;

    @Column(columnDefinition = "varchar(20) comment '级别'")
    private String level;

    @Column(columnDefinition = "decimal(8,4) comment '奖励率'")
    private BigDecimal rewardRate;

    @Column(columnDefinition = "int(11) comment '状态,是否有效 0否，1是'")
    private BooleanEnum status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    @CreationTimestamp
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    @UpdateTimestamp
    private Date updateTime;

    @Column(columnDefinition = "varchar(24) comment '币种unit名称，如：CNYT'")
    private String symbol;
}
