package com.spark.bitrade.dto;

import lombok.Data;

/**
 * C2C订单即将超时，权限内容
 *
 * @author zhongxj
 * @date 2019.09.11
 */
@Data
public class ExpireOtcOrderDto {
    /**
     * 已取消次数
     */
    private Integer otcOrderCancle;
    /**
     * 时间范围内，原始单位：分钟，这里转换为：小时，且遇到除不尽的情况，则自动进一位
     */
    private Integer timeRange;
    /**
     * 时间范围内，取消次数
     */
    private Integer timeRangeOtcOrderCancle;
    /**
     * 冻结权限
     */
    private String accessControl;
}
