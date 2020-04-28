package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BusinessApplyStatus;
import com.spark.bitrade.constant.IncomeType;
import com.spark.bitrade.constant.SettlementType;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlockCoinDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @Column(columnDefinition = "bigint(20) comment '关联锁仓记录ID'")
    private long lockCoinDetailId ;

    @Column(columnDefinition = "decimal(18,8) comment '解锁币数'")
    private BigDecimal amount;

    @Column(columnDefinition = "decimal(18,8) comment '解锁价格（相对USDT）'")
    private  BigDecimal price;

    @Column(columnDefinition = "decimal(18,8) comment '剩余锁仓币数'")
    private BigDecimal remainAmount;

    @CreationTimestamp
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '解锁时间'")
    private Date createTime ;

    @Column(columnDefinition = "varchar(8) comment '结算类型'")
    private SettlementType settlementType;

    @Column(columnDefinition = "decimal(18,8) comment '结算币数'")
    private BigDecimal settlementAmount;

    @Column(columnDefinition = "decimal(18,8)  comment 'USDT价格（CNY）'")
    private BigDecimal usdtPriceCNY;

    @Column(columnDefinition = "varchar(8)  comment '收益类型'")
    private IncomeType incomeType;//收益类型
}
