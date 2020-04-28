package com.spark.bitrade.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BettingRecordStatus;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/***
 * 投注记录表
 * @author yangch
 * @time 2018.09.13 10:44
 */

@Entity
@Data
@Table(name = "pg_betting_record")
public class BettingRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //id,期数id
    private Long periodId;

    //投注时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date betTime;

    //投注币种
    @Column(columnDefinition = "varchar(32) comment '投注币种'")
    private String betSymbol;

    //投注数量
    @Column(columnDefinition = "decimal(18,8) ")
    private BigDecimal betNum;

    //价格范围区间
    private Long rangeId;

    //竞猜币种
    @Column(columnDefinition = "varchar(32) comment '竞猜币种'")
    private String guessSymbol;

    //是否订阅开奖短信提醒
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum useSms;

    //会员ID
    private Long memberId;

    //会员推荐码
    @Column(columnDefinition = "varchar(32) comment '会员推荐码'")
    private String promotionCode;

    //活动状态
    @Enumerated(EnumType.ORDINAL)
    private BettingRecordStatus status;

    //投注用户的受邀用户ID
    @Transient
    private Long inviterId;

    //用户手机号
    @Transient
    private String phone;

    //开始范围
    @Transient
    private BigDecimal beginRange;

    //结束范围
    @Transient
    private BigDecimal endRange;

    //国家区域id
    @Transient
    private String areaCode;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}