package com.spark.bitrade.entity;

import com.spark.bitrade.constant.UttMemberStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 导入用户表
 * </p>
 *
 * @author qiliao
 * @since 2019-08-15
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LockUttMember implements Serializable {

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
     * 批次号
     */
    @Column(columnDefinition = "varchar(255) comment '批次号'")
    private String batchNum;

    /**
     * 锁仓金额
     */
    @Column(columnDefinition = "decimal(18,8) comment '锁仓金额'")
    private BigDecimal amount;

    /**
     * 状态 0: 待处理 1:已处理 2:处理失败
     */
    @Column(columnDefinition = "int(11) comment '0: 待处理 1:已处理 2:处理失败'")
    @Enumerated(EnumType.ORDINAL)
    private UttMemberStatus status;

    /**
     * 创建时间
     */
    @Column(columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(columnDefinition = "datetime comment '更新时间'")
    private Date updateTime;

    /**
     * 备注
     */
    @Column(columnDefinition = "varchar(255) comment '备注'")
    private String remark;

    public static final String ID = "id";

    public static final String MEMBER_ID = "member_id";

    public static final String BATCH_NUM = "batch_num";

    public static final String AMOUNT = "amount";

    public static final String STATUS = "status";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String REMARK="remark";
}
