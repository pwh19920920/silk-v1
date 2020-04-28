package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ActivitieNumType;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author rongyu
 * @description 会员钱包
 * @date 2018/1/2 15:28
 */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberWallet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    /**
     * 自动建立外建，请勿手动添加（如有疑问请，参照spring data jpa spring.jpa.hibernate.ddl-auto=update）
     */
    private Coin coin;
    /**
     * 可用余额
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '可用余额'")
    private BigDecimal balance;
    /**
     * 冻结余额
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '冻结余额'")
    private BigDecimal frozenBalance;

    /**
     * 锁仓余额
     */
    @Column(columnDefinition = "decimal(18,8) default 0 comment '锁仓余额'")
    private BigDecimal lockBalance;

    /**
     * 充值地址
     */
    private String address;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 钱包是否锁定，0否，1是。锁定后
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '钱包是否锁定'")
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;

    //add by yangch 时间： 2018.08.03 原因：
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 1 comment '启动充值，0=禁用/1=启用'")
    private BooleanEnum enabledIn = BooleanEnum.IS_TRUE;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 1 comment '启动提币，0=禁用/1=启用'")
    private BooleanEnum enabledOut = BooleanEnum.IS_TRUE;

}
