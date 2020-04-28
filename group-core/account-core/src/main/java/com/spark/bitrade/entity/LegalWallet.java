package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 会员合法钱包 人民币 新加坡币
 */
//del by yangch 时间： 2018.04.21 原因：代码同步时发现该类已删除
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalWallet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotNull
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;
    @Column(columnDefinition = "decimal(18,8) comment '余额'")
    private BigDecimal rmb = BigDecimal.ZERO;
    /**
     * 新加坡币
     */
    @Column(columnDefinition = "decimal(18,8) comment '余额'")
    private BigDecimal singaporeCurrency = BigDecimal.ZERO;

    public LegalWallet() {
    }

    public LegalWallet(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "LegalWallet{" +
                "id=" + id +
                ", member=" + member +
                ", rmb=" + rmb +
                ", singaporeCurrency=" + singaporeCurrency +
                '}';
    }
}
