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
 * 红包信息活动管理表（red_pack_manage）
 * </p>
 *
 * @author qiliao
 * @since 2019-11-25
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RedPackManage implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 活动id
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 活动名称
     */
    private String redpackName;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 活动币种
     */
    private String unit;

    /**
     * 预算币种总数量
     */
    private BigDecimal totalAmount;

    /**
     * 红包总份数
     */
    private Integer totalCount;

    /**
     * 领取模式{1:随机数量,2:固定数量}
     */
    private Integer receiveType;

    /**
     * 红包数量最小值
     */
    private BigDecimal minAmount;

    /**
     * 红包数量最大值
     */
    private BigDecimal maxAmount;

    /**
     * 剩余数量
     */
    private BigDecimal redPacketBalance;

    /**
     * 红包时限小时
     */
    private Integer within;

    /**
     * 限用户类型参与活动{0:所有,1:新会员, 2:老会员}
     */
    private Integer isOldUser;

    /**
     * 首页弹出优先级
     */
    private Integer priority;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建用户id
     */
    private Long createUserid;

    /**
     * 修改用户
     */
    private Long updateUserid;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer deleteFlag;

    /**
     * url
     */
    private String url;

    /**
     * 红包剩余份数
     */
    private Integer surplusCount;

}
