package com.spark.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.TransactionType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @desc 会员交易记录，包括充值、提现、转账等
 *
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberTransaction {
    @Excel(name = "交易记录编号", orderNum = "1", width = 25)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Excel(name = "会员id", orderNum = "2", width = 25)
    private Long memberId;
    /**
     * 交易金额
     */
    @Excel(name = "交易金额", orderNum = "3", width = 25)
    @Column(columnDefinition = "decimal(18,8) comment '充币金额'")
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @Excel(name = "创建时间", orderNum = "4", width = 25)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 交易类型
     */
    @Excel(name = "交易类型", orderNum = "5", width = 25)
    @Enumerated(EnumType.ORDINAL)
    private TransactionType type;
    /**
     * 币种名称，如 BTC
     */
    private String symbol;
    /**
     * 充值或提现地址、或转账地址
     */
    private String address;

    /**
     * 交易手续费
     * 提现和转账才有手续费，充值没有;如果是法币交易，只收发布广告的那一方的手续费
     */
    @Column(precision = 19,scale = 8)
    private BigDecimal fee = BigDecimal.ZERO ;

    /**
     * 标识位，特殊情况会用到，默认为0
     */
    @Column(nullable=false,columnDefinition="int default 0")
    private int flag = 0;

    //add by yangch 时间： 2018.05.16 原因：关联单号，币币交易关联exchange_order.order_id字段
    /**
     * 关联单号
     */
    private String refId;

    //add by shenzucai 时间： 2018.05.25 原因：用于人工充值memberid
    /**
     * 备注，目前用于人工充值
     */
    private String comment;

    //优惠手续费
    @Column(columnDefinition = "decimal(18,8) default 0 comment '优惠手续费'")
    private BigDecimal feeDiscount;

    //add by  shenzucai 时间： 2018.10.29  原因： 匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 start
    @Column(columnDefinition = "varchar(255) comment '手续费抵扣币种单位（不包括当前币种）'")
    private String feeDiscountCoinUnit;

    @Column(columnDefinition = "decimal(18,8) default 0 comment '抵扣币种对应手续费'")
    private BigDecimal feeDiscountAmount;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end
}
