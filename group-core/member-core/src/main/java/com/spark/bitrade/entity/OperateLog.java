package com.spark.bitrade.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 操作记录
 * </p>
 *
 * @author qiliao
 * @since 2019-10-14
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OperateLog implements Serializable {

    private static final long serialVersionUID=1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 操作类型 {1:交易状态,2:账户状态,3:登录谷歌认证,4:提币谷歌认证,5:登录手机认证,6:提币手机认证,7:场外交易（买入）,8:场外交易（卖出）,9:币币交易,10:提币,11:平台内部转账,12:充值管理,13:提币管理,14:钱包状态,15:手机号邮箱修改}
     */
    private Integer operateType;

    /**
     * 开关类型 {0:关,1:开}
     */
    private Integer switchType;

    /**
     * 币种
     */
    private String coinUnit;

    /**
     * 自动/手动 {0:自动,1:手动}
     */
    private Integer autoManal;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;



}
