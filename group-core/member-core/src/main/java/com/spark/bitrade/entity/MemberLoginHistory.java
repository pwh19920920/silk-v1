package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.LoginType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 会员登录历史
  * @author tansitao
  * @time 2018/7/10 9:43 
  */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberLoginHistory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private long memberId;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '登录类型（0:表示WEB;1:表示android;2:表示IOS）'")
    private LoginType type;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '登录时间'")
    private Date loginTime;

    @Column(columnDefinition = "varchar(32)  comment '登录IP'")
    private String loginIP;

    @Column(columnDefinition = "varchar(16)  comment '区域信息'")
    private String area;

    //add by tansitao 时间： 2018/12/27 原因：增加的设备信息
    //0：表示星客交易所会员;1：表示升腾会员;2：表示链人会员;3：表示星客CNYT理财会员;4：表示星客钱包会员
    @Column(columnDefinition = "varchar(64)  comment '第三方平台标志'")
    private String thirdMark;

    @Column(columnDefinition = "varchar(8)  comment '是否为注册信息，0：表示登录信息;1：表示注册信息'")
    private BooleanEnum isRegistrate;

    @Column(columnDefinition = "varchar(256)  comment '厂商'")
    private String producers;

    @Column(columnDefinition = "varchar(256)  comment '系统版本'")
    private String systemVersion;

    @Column(columnDefinition = "varchar(256)  comment '设备型号'")
    private String model;

    @Column(columnDefinition = "varchar(256)  comment '唯一标志码UUID'")
    private String uuid;

    @Column(columnDefinition = "varchar(8)  comment '手机是否root或越狱'")
    private BooleanEnum isRootOrJailbreak;

}
