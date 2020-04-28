package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PaymentCodeStrategyType;
import lombok.Data;

/**
 *  付款码功能管理
 *
 * @author yangch
 * @time 2019.03.01 11:13
 */

@Data
public class PayPaymentCodeManage {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 平台ID
     */
    private String appId;

    /**
     * 策略类型 1、定时自动刷新 2、每次用后失效
     */
    private PaymentCodeStrategyType strategyType;

    /**
     * 付款码终端信息，每个用户只能对一台手机开通付款
     */
    private String terminalDeviceInfo;

    /**
     * 开启标记 (1:启用，0：禁用)
     */
    private BooleanEnum enabled;

    /**
     * 定时自动刷新策略有效时间，单位为秒，默认为60秒
     */
    private Integer strategyEffectiveTime;
}
