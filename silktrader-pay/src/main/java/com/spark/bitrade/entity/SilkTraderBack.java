package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.SilkPayBackStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 三方支付退款记录
 * @author shenzucai
 * @time 2018.07.01 15:42
 */
@Entity
@Data
@ExcelSheet
public class SilkTraderBack {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 退款账号 涉及到平台总账号，平台用户账号 平台商户账号
     */
    @Excel(name = "退款账号")
    @Column(columnDefinition = "varchar(255) comment '退款账号'")
    private String backAccount;

    /**
     * 支付账号 涉及到平台总账号，平台商户账号 平台用户账号
     */
    @Excel(name = "支付账号")
    @Column(columnDefinition = "varchar(255) comment '支付账号'")
    private String payAccount;

    /**
     * 三方支付交易id
     */
    @Excel(name = "三方支付交易id")
    @Column(columnDefinition = "bigint(20) comment '三方支付交易id'")
    private Long transactionId;

    /**
     * 退款数量
     */
    @Excel(name = "退款数量")
    @Column(columnDefinition = "decimal(18,8) comment '退款数量'")
    private BigDecimal backAmount;

    /**
     * 退款币种
     */
    @Excel(name = "退款币种")
    @Column(columnDefinition = "decimal(18,8) comment '退款币种'")
    private BigDecimal backCoin;

    /**
     * 退款状态
     */
    @Excel(name = "退款状态")
    @Column(columnDefinition = "int(4) comment ' 退款状态 0退款中，1已退款，2退款失败'")
    @Enumerated(EnumType.ORDINAL)
    private SilkPayBackStatus status = SilkPayBackStatus.BACKING;
    /**
     * 订单时间（yyyy-MM-dd HH:mm:ss）
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '订单时间'")
    private Date orderTime;

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

}
