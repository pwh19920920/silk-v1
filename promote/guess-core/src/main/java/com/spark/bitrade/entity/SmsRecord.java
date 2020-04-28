package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.RewardBusinessType;
import com.spark.bitrade.constant.RewardStatus;
import com.spark.bitrade.constant.SmsStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短信状态
 * @author tansitao
 * @time 2018/9/19 16:01 
 */

@Entity
@Data
@Table(name = "sms_record")
public class SmsRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private String phone;

    //发送状态：待发送、成功、失败
    @Enumerated(EnumType.ORDINAL)
    private SmsStatus sendStatus;
    //短信发送时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
     private Date sendTime;

    //定时时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date taskTime;

    @Column(columnDefinition = "varchar(512) comment '原因'")
    private String reason;

    @Column(columnDefinition = "varchar(512) comment '短信内容'")
    private String smsContent;

    //关联业务id
    private String refId;

    @Column(columnDefinition = "varchar(512) comment '备注'")
    private String remark;

}