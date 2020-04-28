package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.AlarmType;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 告警监控实体
 * @author Zhang Yanjun
 * @time 2018.09.26 16:32
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmMonitor {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long memberId;//告警用户

    @Enumerated(EnumType.ORDINAL)
    private AlarmType alarmType;//告警类型 0未知  1币币交易撤单

    @Column(columnDefinition = "varchar(4096) comment '告警内容'")
    private String alarmMsg;//告警内容

    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum status;//是否处理  0未处理  1已处理

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date alarmTime;//告警时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date maintenanceTime;//告警处理时间

    @Column(columnDefinition = "varchar(4096) comment '处理意见'")
    private String maintenanceMsg;//处理意见


    private Long maintenanceId;//处理人id，管理后端admin表
    @Transient
    private String maintenanceRealName;//处理人真实姓名
}
