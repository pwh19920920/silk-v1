package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 用户权限自动解禁任务表
 * @author tansitao
 * @time 2018/11/27 11:48 
 */

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberPermissionsRelieveTask {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    //解禁权限类型
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '解禁权限类型'")
    private MonitorExecuteEnvent relievePermissionsType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '计划解锁时间'")
    private Date relieveTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '解冻权限任务状态（待处理，已处理）'")
    private RelievePermissionsStaus status = RelievePermissionsStaus.unProcessed;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '处理时间'")
    private Date dealTime;

    @Column(columnDefinition = "int comment '可用标识:0:不可用，1:可用'")
    private BooleanEnum usable = BooleanEnum.IS_TRUE;

    @Column(columnDefinition = "bigint(20) comment '关联的警告ID'")
    private Long alarmMonitorId;

}
