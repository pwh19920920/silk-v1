package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.ProcessStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 锁仓活动解锁任务
 * @author yangch
 * @time 2018.08.02 11:37
 */
@Data
@Entity
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlockCoinTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    //锁仓类型（锁仓活动、锁仓充值）
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "varchar(8) comment '锁仓类型（商家保证金、员工锁仓、锁仓活动、理财锁仓、SLB节点产品）'")
    private LockType type;

    @Column(columnDefinition = "bigint(20) comment '关联活动ID'")
    private Long refActivitieId;

    @Column(columnDefinition = "decimal(18,8) comment '解锁触发价'")
    private BigDecimal price;


    //状态（未处理，处理中，已处理）
    private ProcessStatus status;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime comment '任务创建时间'")
    private Date createTime ;

    //备注
    private String note;
}
