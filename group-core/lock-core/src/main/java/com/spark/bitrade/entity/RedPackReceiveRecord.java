package com.spark.bitrade.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 红包领取记录(red_pack_receive_record)
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RedPackReceiveRecord implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 活动ID
     */
    private Long redpackId;

    /**
     * 活动名称
     */
    private String redpackName;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 领取币种
     */
    private String receiveUnit;

    /**
     * 领取数量
     */
    private BigDecimal receiveAmount;

    /**
     * 领取状态{0:未领取1:已领取,:已收回}
     */
    private Integer receiveStatus;

    /**
     * 领取时间
     */
    private Date receiveTime;

    /**
     * 用户类型:{1:新会员,2:游客, 3:老会员}
     */
    private Integer userType;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 领取限时
     */
    private Integer within;
}
