package com.spark.bitrade.dto;

import com.spark.bitrade.constant.LockStatus;
import com.spark.bitrade.constant.LockType;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by lingxing on 2018/7/12.
 */
@Setter
@Getter
public class LockAbstractDto {
    private Integer type;//类型
    private Integer status;//状态
    private String coinUnit;//币种
    private String userName;//用户名
    private String  lockTypeStatus;//internalDetail、detail
    private String startTime;
    private String endTime;
    private String startLockTime;
    private String endLockTime;
}
