package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.constant.MonitorExecuteEnvent;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import lombok.Data;

import javax.persistence.*;

/***
 * 平台用户监控规则配置
 * @author yangch
 * @time 2018.11.01 9:11
 * @since 1.3版本新增
 * @version
 */

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitorRuleConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //触发事件
    @Enumerated(EnumType.ORDINAL)
    private MonitorTriggerEvent triggerEvent;

    //时间段（单位：分钟）
    private int triggerStageCycle;

    //触发次数
    private int triggerTimes;

    //用户类别
    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum triggerUserLevel;

    //执行的约束
    @Enumerated(EnumType.ORDINAL)
    private MonitorExecuteEnvent executeEvent;

    //约束的有效时长（单位：分钟）
    private int executeDuration;
}
