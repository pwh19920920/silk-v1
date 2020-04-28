package com.spark.bitrade.entity;

import lombok.Data;
import javax.persistence.*;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigDecimal;

/***
 * 投注配置价格区间表
 * @author yangch
 * @time 2018.09.13 10:41
 */

@Entity
@Data
@Table(name = "pg_betting_price_range")
public class BettingPriceRange {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    //组名
    private String groupName;

    //开始范围
    @Column(columnDefinition = "decimal(18,8) ")
    private BigDecimal beginRange;

    //结束范围
    @Column(columnDefinition = "decimal(18,8) ")
    private BigDecimal endRange;

    //排序
    private Integer orderId;

    //数量
    @Transient
    private BigDecimal number = BigDecimal.ZERO;

}