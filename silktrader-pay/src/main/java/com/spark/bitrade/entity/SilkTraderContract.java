package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.ContractEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付签约实体
 * @author shenzucai
 * @time 2018.07.01 15:42
 */
@Entity
@Data
@ExcelSheet
public class SilkTraderContract {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 签约编号
     */
    @Excel(name = "签约编号")
    @Column(columnDefinition = "varchar(255) comment '签约编号'")
    private String contractNo;
    /**
     * 商户账号
     */
    @Excel(name = "商户账号")
    @Column(columnDefinition = "varchar(255) comment '商户账号'")
    private String busiAccount;
    /**
     * 通讯秘钥
     */
    @Excel(name = "通讯秘钥")
    @Column(columnDefinition = "varchar(255) comment '通讯秘钥'")
    private String messageKey;

    /**
     * 折扣率
     */
    @Excel(name = "折扣率")
    @Column(columnDefinition = "decimal(8,4) comment '折扣率'")
    private BigDecimal discount;
    /**
     * 启用状态
     */
    @Excel(name = "启用状态")
    @Column(columnDefinition = "int(4) comment '启用状态0禁用，1启用'")
    @Enumerated(EnumType.ORDINAL)
    private ContractEnum status = ContractEnum.IS_FALSE;


    /**
     * 异步通知url
     */
    @Excel(name = "异步通知url")
    @Column(columnDefinition = "varchar(255) comment '异步通知url'")
    private String asyncNotifyUrl;

    /**
     * 签约起始时间（yyyy-MM-dd HH:mm:ss）
     */
    @Excel(name = "签约起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '签约起始时间'")
    private Date contractStart;


    /**
     * 签约周期
     */
    @Excel(name = "签约周期")
    @Column(columnDefinition = "bigint(20) comment '签约周期'")
    private Long period;

    /**
     * 订单超时时间
     */
    @Excel(name = "订单超时时间")
    @Column(columnDefinition = "bigint(20) comment '订单超时时间  分钟数 -1为不超时'")
    private Long expireTime;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    /**
     * 修改时间（yyyy-MM-dd HH:mm:ss）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '修改时间'")
    private Date updateTime;

    /**
     * 备注
     */
    @Excel(name = "备注")
    @Column(columnDefinition = "varchar(255) comment '备注'")
    private String comment;

    /**
     * 商家主页访问地址
     */
    @Column(columnDefinition = "varchar(255) comment '商家主页访问地址'")
    private String busiUrl;

    /**
     * 签约渠道
     */
    @Column(columnDefinition = "bigint(20) comment '签约渠道（关联申请id，如果没有则为人工处理）'")
    private Long applyId;

}
