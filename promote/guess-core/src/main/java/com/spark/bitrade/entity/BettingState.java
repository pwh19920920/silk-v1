package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BettingStateOperateType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/***
 * 后端任务分配标记表
 * @author yangch
 * @time 2018.09.14 10:33
 */

@Entity
@Data
@Table(name = "pg_betting_state")
public class BettingState {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    //操作类型(分红返佣,回购,奖励,红包扣除,下期奖池沉淀扣除,抢红包,红包领取时间结束)
    @Enumerated(EnumType.ORDINAL)
    private BettingStateOperateType operate;

    //标记(未处理,处理中,已处理)
    @Enumerated(EnumType.ORDINAL)
    private BettingStateOperateMark mark;

    //创建时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //更新时间
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
