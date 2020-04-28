package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockRewardSatus;
import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.SmsSendStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
  * 用户支付账户
  * @author tansitao
  * @time 2018/8/13 17:39 
  */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberPaymentAccount {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private long memberId;

    @Column(columnDefinition = "varchar(64) comment 'Epay账户'")
    private String epayNo;

    //add by tansitao 时间： 2018/11/9 原因：增加账户姓名
    @Column(columnDefinition = "varchar(128) comment '用户账户姓名'")
    private String accountName;
}
