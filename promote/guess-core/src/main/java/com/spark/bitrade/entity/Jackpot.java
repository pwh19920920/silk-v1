package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 奖池记录表
 * @author yangch
 * @time 2018.09.13 11:18
 */

@Entity
@Data
@Table(name = "pg_jackpot")
public class Jackpot {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    //中奖价格
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal prizePrice;

//    //当期奖池余量,不变
//    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
//    private BigDecimal jackpotBalanceFixed;
//
//    //当期红包余量,不变
//    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
//    private BigDecimal redpacketBalanceFixed;

    //当期奖池余量,不变
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal jackpotBalance;

    //当期红包余量,不变
    @Column(columnDefinition = "decimal(18,8) DEFAULT 0 ")
    private BigDecimal redpacketBalance;

    //奖励币种
    @Column(columnDefinition = "varchar(32) comment '奖励币种'")
    private String prizeSymbol;

    //红包币种
    @Column(columnDefinition = "varchar(32) comment '红包币种'")
    private String redpacketSymbol;

    //价格范围区间
    private Long rangeId;

    //创建时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date happenTime;

    //价格开始范围
    @Transient
    private BigDecimal beginRange;

    //价格结束范围
    @Transient
    private BigDecimal endRange;

    public void setPrizeSymbol(String prizeSymbol) {
        this.prizeSymbol = prizeSymbol == null ? null : prizeSymbol.trim();
    }

    public void setRedpacketSymbol(String redpacketSymbol) {
        this.redpacketSymbol = redpacketSymbol == null ? null : redpacketSymbol.trim();
    }
}