package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.util.MD5Util;
import com.spark.bitrade.util.UUIDUtil;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 三方平台支付商户申请表
 * @author fumy
 * @time 2018.08.03 11:13
 */
@Entity
@Data
public class ThirdPlatformApply {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 商户silkTrader平台账号
     */
    @Column(columnDefinition = "varchar(255) comment '平台商户账号'")
    private String busiAccount;

    /**
     * 法币类型
     */
    @Column(columnDefinition = "varchar(255) comment '法币类型'")
    private String currency;

    /**
     * 签约币种
     */
    @Column(columnDefinition = "varchar(255) comment '签约币种'")
    private String contractCoin;

    /**
     * 入账币种
     */
    @Column(columnDefinition = "varchar(255) comment '入账币种'")
    private String busiCoin;

    /**
     * 手续费率
     */
    @Column(columnDefinition = "decimal(8,4) comment '手续费率'")
    private BigDecimal busiCoinFeeRate;

    /**
     * 异步通知url
     */
    @Column(columnDefinition = "varchar(255) comment '异步通知url'")
    private String asyncNotifyUrl;

    /**
     * 签约周期
     */
    @Column(columnDefinition = "int(11) comment '签约周期（负数代表长期）天数'")
    private int period;

    /**
     * 订单超时时间
     */
    @Column(columnDefinition = "bigint(20) comment '订单超时时间  分钟数 -1为不超时'")
    private Long expireTime;

    /**
     * 备注（审核不通过时需要填写）
     */
    @Column(columnDefinition = "varchar(255) comment '备注'")
    private String comment;

    /**
     *  申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '申请时间'")
    private Date applyTime;

    /**
     * 审核状态，0:待审核，1：审核未通过，2：审核通过
     */
    @Column(columnDefinition = "tinyint(4) comment '0:待审核，1：审核未通过，2：审核通过'")
    private int status=0;

    /**
     * 申请key
     */
    @Column(columnDefinition = "varchar(64) comment '申请时提交的第三方合作平台applyKey'")
    private String applyKey;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    private Date lastTime;

}
