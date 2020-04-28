package com.spark.bitrade.entity;

import com.spark.bitrade.constant.LockCoinRechargeThresholdType;
import com.spark.bitrade.constant.LockStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 锁仓记录
 * @author tansitao
 * @time 2018/6/20 15:52 
 */
@Data
public class UnLockCoinDetailBuilder {
    private long id;
    private String symbol; //币种
    private BigDecimal lockNum; //锁仓数量
    private Date lockTime; //锁仓时间
    private BigDecimal lockPrice;//锁仓价格
    private LockCoinRechargeThresholdType thresholdType; //阀值类型(锁仓类型)
    private int unlockNum;//解锁次数
    private BigDecimal lastUnLockPrice;//最近一次解锁价格
    private Date lastUnlockTime;//最近一次解锁时间
    private BigDecimal remainAmount;//剩余锁仓币数
    private LockStatus status;//状态
    private String note;//备注

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getLockNum() {
        return lockNum;
    }

    public void setLockNum(BigDecimal lockNum) {
        this.lockNum = lockNum;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public BigDecimal getLockPrice() {
        return lockPrice;
    }

    public void setLockPrice(BigDecimal lockPrice) {
        this.lockPrice = lockPrice;
    }

    public LockCoinRechargeThresholdType getThresholdType() {
        return thresholdType;
    }

    public void setThresholdType(LockCoinRechargeThresholdType thresholdType) {
        this.thresholdType = thresholdType;
    }

    public int getUnlockNum() {
        return unlockNum;
    }

    public void setUnlockNum(int unlockNum) {
        this.unlockNum = unlockNum;
    }

    public BigDecimal getLastUnLockPrice() {
        return lastUnLockPrice;
    }

    public void setLastUnLockPrice(BigDecimal lastUnLockPrice) {
        this.lastUnLockPrice = lastUnLockPrice;
    }

    public Date getLastUnlockTime() {
        return lastUnlockTime;
    }

    public void setLastUnlockTime(Date lastUnlockTime) {
        this.lastUnlockTime = lastUnlockTime;
    }

    public BigDecimal getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(BigDecimal remainAmount) {
        this.remainAmount = remainAmount;
    }

    public LockStatus getStatus() {
        return status;
    }

    public void setStatus(LockStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
