package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.AppealType;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * 申诉
 *
 * @author Zhang Jinwei
 * @date 2018年01月22日
 */
@Entity
@Data
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class Appeal {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @OneToOne
    private Order order;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 处理时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealWithTime;

    @Column(length = 500)
    @ApiModelProperty(value = "申诉缘由",name = "remark")
    private String remark;

    /**
     * 申诉发起者id
     */
    private Long initiatorId;
    /**
     * 申诉关联者id
     */
    private Long associateId;

    /**
     * 发起者是否胜诉
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = true)
    private BooleanEnum isSuccess ;
    /**
     * 处理状态
     */
    @Enumerated(EnumType.ORDINAL)
    private AppealStatus status = AppealStatus.NOT_PROCESSED;

    /**
     * 处理者
     */
    @JoinColumn(name = "admin_id")
    @ManyToOne
    private Admin admin;


    //>>> add by zyj 2018-11-21 pc1.3.1新加字段 start
    /**
     * 取消者id
     */
    @Column(columnDefinition = "int(11) comment '申诉取消者id'")
    private Long cancelId;
    /**
     * 取消原因
     */
    @Column(columnDefinition = "int(11) comment '申诉取消原因 0已经联系上卖家，等待卖家放币，1卖家已确认到账，等待卖家放币，2买家已付款，3其他'")
    private Integer cancelReason;

    /**
     * 取消原因描述
     */
    @Column(columnDefinition = "text comment '申诉取消原因描述'")
    private String cancelDescription;

    /**
     * 申诉取消时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;
    //<<< add by zyj 2018-11-21 pc1.3.1新加字段 end

    // >>> add by zyj 2018.11.1 pc1.3.0版本增加字段
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) comment '申诉类型 0请求放币，1请求取消订单，2其他'")
    @ApiModelProperty(value = "申诉类型 0请求放币，1请求取消订单，2其他",name = "appealType")
    private AppealType appealType;


    @Column(length = 500)
    @ApiModelProperty(value = "胜诉缘由",name = "successRemark")
    private String successRemark;
    //<<< add by zyj 2018.11.1  pc1.3.0版本增加字段结束
}
