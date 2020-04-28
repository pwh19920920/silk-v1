package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.LockStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 锁仓活动
 * @author tansitao
 * @time 2018/6/20 15:52 
 */
@Builder
@Data
public class LockActivitySettingBuilder {
    private long id;
    private String name; //活动名称
    private String symbol; //币种

    private BigDecimal unitPerAmount;//活动每份数量（1表示1个币，大于1表示每份多少币）
    private BigDecimal planAmount;//活动计划数量（币数、份数）
    private BigDecimal boughtAmount;//活动参与数量（币数、份数）
    private BigDecimal minBuyAmount;//最低购买数量（币数、份数）
    private BigDecimal maxBuyAmount;//最大购买数量（币数、份数）

    private BigDecimal planIncome; //预期收益
    private BigDecimal earningRate; //年化率
    private double cycle; //周期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime; //活动开始时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime; // 活动截止时间

}
