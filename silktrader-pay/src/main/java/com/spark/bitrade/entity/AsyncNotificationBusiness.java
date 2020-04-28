package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 异步业务通知
 *
 * @author daring5920
 * @time 2019/7/30 10:44
 */
@Data
@Builder
@TableName("async_notification_business")
@NoArgsConstructor
@AllArgsConstructor
public class AsyncNotificationBusiness {

    /**
     * '主键id'
     */
    private Long id;
    /**
     * '三方业务id'
     */
    private String orderId;
    /**
     * '交易id'
     */
    private String traderSn;
    /**
     * '业务状态'
     */
    private String status;
    /**
     * '业务标志'
     */
    private String tag;

    /**
     * 0 买入，1卖出，默认0
     */
    private Integer type;
    /**
     * '应用id'
     */
    private String appId;

    /**
     * 创建时间
     */
    private Date createTime;

}
