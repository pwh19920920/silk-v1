package com.spark.bitrade.entity;

import com.spark.bitrade.constant.LockStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商家锁仓记录查询实体类
 * @author fumy
 * @time 2018.06.21 14:27
 */
@Data
public class CustomerLockCoinDetail{

    private Long id;

    private String coinUnit;

    private BigDecimal totalAmount;

    /**
     * 剩余锁仓币数
     */
    private BigDecimal remainAmount;

    private Date lockTime;

    private Date unLockTime;

    private int lockDays;

    private LockStatus status;

    public CustomerLockCoinDetail(){};

    public CustomerLockCoinDetail(Long id, String coinUnit, BigDecimal totalAmount,BigDecimal remainAmount, Date lockTime, Date unLockTime, LockStatus status) {
        this.id = id;
        this.coinUnit = coinUnit;
        this.totalAmount = totalAmount;
        this.remainAmount = remainAmount;
        this.lockTime = lockTime;
        this.unLockTime = unLockTime;
        this.status = status;
    }
}
