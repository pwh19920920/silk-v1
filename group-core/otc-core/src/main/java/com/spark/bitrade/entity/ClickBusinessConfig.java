package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 一键商家买卖币配置
 * @author tansitao
 * @time 2019.01.03 17:22
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickBusinessConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    @Column(columnDefinition = "varchar(32) comment '商家手机号'")
    private String mobilePhone;

    @Column(columnDefinition = "varchar(255) comment '邮箱'")
    private String email;

    @Column(columnDefinition = "varchar(255) comment '用户昵称'")
    private String username;

    @Column(columnDefinition = "varchar(255) comment '真实姓名'")
    private String realName;

    @CreationTimestamp
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @UpdateTimestamp
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '修改时间'")
    private Date updateTime;

    @Column(columnDefinition = "int(11) comment '数据是否可用'")
    private BooleanEnum usable = BooleanEnum.IS_TRUE;

    @Column(columnDefinition = "VARCHAR(50) COMMENT '币种类型'")
    private String unit;

    @Column(columnDefinition = "VARCHAR(60) COMMENT '所属应用ID'")
    private String appId;
}
