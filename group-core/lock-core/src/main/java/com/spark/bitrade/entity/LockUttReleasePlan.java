package com.spark.bitrade.entity;

import com.spark.bitrade.constant.UttReleaseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * UTT释放计划表
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LockUttReleasePlan implements Serializable {

    private static final long serialVersionUID=1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 用户id
     */
    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private Long memberId;

    /**
     * 锁仓记录id
     */
    @Column(columnDefinition = "bigint(20) comment '锁仓记录id'")
    private Long lockDetailId;

    /**
     * 创建时间
     */
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    /**
     * 计划释放时间
     */
    @Column(columnDefinition = "datetime comment '计划释放时间'")
    private Date planUnlockTime;

    /**
     * 币种
     */
    @Column(columnDefinition = "varchar(20) comment '币种'")
    private String coinUnit;

    /**
     * 解锁数量
     */
    @Column(columnDefinition = "decimal(18,8) comment '锁仓金额'")
    private BigDecimal unlockAmount;

    /**
     * 释放状态 0:待释放 1:已释放 2:已撤销
     */
    @Column(columnDefinition = "int(11) comment '0:待释放 1:已释放 2:已撤销'")
    @Enumerated(EnumType.ORDINAL)
    private UttReleaseStatus status;

    /**
     * 备注
     */
    @Column(columnDefinition = "varchar(20) comment '备注'")
    private String remark;

    /**
     * 更新时间
     */
    @Column(columnDefinition = "datetime comment '更新时间'")
    private Date updateTime;


    public static final String ID = "id";

    public static final String MEMBER_ID = "member_id";

    public static final String LOCK_DETAIL_ID = "lock_detail_id";

    public static final String CREATE_TIME = "create_time";

    public static final String PLAN_UNLOCK_TIME = "plan_unlock_time";

    public static final String COIN_UNIT = "coin_unit";

    public static final String UNLOCK_AMOUNT = "unlock_amount";

    public static final String STATUS = "status";

    public static final String REMARK = "remark";

    public static final String UPDATE_TIME = "update_time";

}
