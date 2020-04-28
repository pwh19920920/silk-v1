package com.spark.bitrade.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.ContractEnum;
import com.spark.bitrade.constant.SilkPayMainBackStatus;
import com.spark.bitrade.constant.SilkPayOrderStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import sun.util.resources.el.CalendarData_el_CY;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录实体
 * @author shenzucai
 * @time 2018.07.01 15:42
 */
@Entity
@Data
@ExcelSheet
public class SilkTraderTransaction {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 签约细节id
     */
    @Excel(name = "签约细节id")
    @Column(columnDefinition = "bigint comment '签约细节id'")
    private String contractDetailId;
    /**
     * 平台商户账号
     */
    @Excel(name = "商户账号")
    @Column(columnDefinition = "varchar(255) comment '商户账号'")
    private String busiAccount;

    /**
     * 平台用户账号（顾客）
     */
    @Excel(name = "顾客账号")
    @Column(columnDefinition = "varchar(255) comment '平台用户账号（顾客）'")
    private String userAccount;

    /**
     * 平台订单号yyyyMMddHHmmssSSS-平台商户id-平台顾客id 理论上一个用户在一个商家下1秒钟下单一千次的可能性接近0
     */
    @Excel(name = "平台订单号")
    @Column(columnDefinition = "varchar(255) comment '平台订单号yyyyMMddHHmmssSSS-平台商户id-平台顾客id 理论上一个用户在一个商家下1秒钟下单一千次的可能性接近0'")
    private String silkOrderNo;

    /**
     * 商户订单号(商家自身平台)
     */
    @Excel(name = "商户订单号")
    @Column(columnDefinition = "varchar(255) comment '商户订单号(商家自身平台)'")
    private String payId;

    /**
     * 商家自身平台用户账号
     */
    @Excel(name = "商家自身平台用户账号")
    @Column(columnDefinition = "varchar(255) comment '商家自身平台用户账号'")
    private String userId;
    /**
     * 订单金额(法币)
     */
    @Excel(name = "订单金额(法币)")
    @Column(columnDefinition = "decimal(18,8) comment '订单金额(法币)'")
    private BigDecimal amount;


    /**
     * 实际支付金额(法币)
     */
    @Excel(name = "实际支付金额(法币)")
    @Column(columnDefinition = "decimal(18,8) comment '实际支付金额(法币)'")
    private BigDecimal actualAmount;

    /**
     * 签约币种价格(例如SLB的USDT价格)
     */
    @Excel(name = "签约币种价格")
    @Column(columnDefinition = "decimal(18,8) comment '签约币种价格(例如SLB的USDT价格)'")
    private BigDecimal contractBusiPrice;

    /**
     * 商家入账币种价格(对应币种的法币价格)
     */
    @Excel(name = "商家入账币种价格")
    @Column(columnDefinition = "decimal(18,8) comment '商家入账币种价格(对应币种的法币价格)'")
    private BigDecimal busiCurrencyPrice;

    /**
     * 签约币数量
     */
    @Excel(name = "签约币数量")
    @Column(columnDefinition = "decimal(18,8) comment '签约币（例如SLB）数量'")
    private BigDecimal contractAmount;

    /**
     * 商家入账币数量
     */
    @Excel(name = "商家入账币数量")
    @Column(columnDefinition = "decimal(18,8) comment '商家入账币(USDT)数量'")
    private BigDecimal busiAmount;

    /**
     * silktrader(专属三方支付)平台账号 用于平账，对账
     */
    @Excel(name = "专属三方支付总账号")
    @Column(columnDefinition = "bigint(20) comment 'silktrader(专属三方支付)平台账号 用于平账，对账'")
    private Long memberId;

    /**
     * 订单状态
     */
    @Excel(name = "订单状态")
    @Column(columnDefinition = "int(4) comment ' 订单状态 0未支付，1已支付，2已取消'")
    @Enumerated(EnumType.ORDINAL)
    private SilkPayOrderStatus status = SilkPayOrderStatus.NONPAYMENT;

    /**
     * 退款状态
     */
    @Excel(name = "退款状态")
    @Column(columnDefinition = "int(4) comment ' 退款状态 0未申请退款，1申请退款，2退款中，3退款完成，4退款失败' ")
    @Enumerated(EnumType.ORDINAL)
    private SilkPayMainBackStatus backStatus = SilkPayMainBackStatus.UNAPPLYBACK;
    /**
     * 订单时间（yyyy-MM-dd HH:mm:ss）
     */
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

    /**
     * 手续费
     */
    @Excel(name = "手续费")
    @Column(columnDefinition = "decimal(18,8) comment '手续费（如SLB）'")
    private BigDecimal fee;
}
