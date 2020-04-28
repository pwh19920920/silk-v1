package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年03月01日
 */
@Builder
@Data
public class ScanWithdrawRecord {

    /**
     * 申请总数量
     */
    private BigDecimal totalAmount;
    /**
     * 手续费
     */
    private BigDecimal fee;
    /**
     * 预计到账数量
     */
    private BigDecimal arrivedAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;
    /**
     * 提现状态
     */
    private WithdrawStatus status ;
    /**
     * 是否是自动提现
     */
    private BooleanEnum isAuto;

    private String unit;

    /** 能不能自动提现 */
    private BooleanEnum canAutoWithdraw;

    /**
     * 交易编号
     */
    private String transactionNumber;

    /**
     * 提现地址
     */
    private String address;

    private String remark;

    //add by shenzucai 时间： 2018.05.25 原因：添加区块链浏览器地址
    /**
     * 对应区块链浏览器tx前缀
     */
    private String transactionlink;

    //add by  shenzucai 时间： 2018.10.29  原因： 匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 start
    /**
     * 手续费抵扣币种单位（不包括当前币种）
     */
    private String feeDiscountCoinUnit;

    /**
     * 抵扣币种对应手续费
     */
    private BigDecimal feeDiscountAmount;
    //add by  shenzucai 时间： 2018.10.29  原因：匹配产品1.3 A9需求，实现使用其他币种抵扣手续费 end


    public static ScanWithdrawRecord toScanWithdrawRecord(WithdrawRecord withdrawRecord) {
        return ScanWithdrawRecord.builder().totalAmount(withdrawRecord.getTotalAmount())
                .createTime(withdrawRecord.getCreateTime())
                .unit(withdrawRecord.getCoin().getUnit())
                .dealTime(withdrawRecord.getDealTime())
                .fee(withdrawRecord.getFee())
                .arrivedAmount(withdrawRecord.getArrivedAmount())
                .status(withdrawRecord.getStatus())
                .isAuto(withdrawRecord.getIsAuto())
                .address(withdrawRecord.getAddress())
                .remark(withdrawRecord.getRemark())
                .canAutoWithdraw(withdrawRecord.getCanAutoWithdraw())
                .transactionNumber(withdrawRecord.getTransactionNumber())
                // 添加前端所需区块链浏览器地址
                .transactionlink(withdrawRecord.getCoin().getExploreUrl())
                .feeDiscountCoinUnit(withdrawRecord.getCoin().getFeeDiscountCoinUnit())
                //add|edit|del by  shenzucai 时间： 2018.11.11  原因：修正金额错误，具体金额已提币记录为准
                .feeDiscountAmount(withdrawRecord.getFeeDiscountAmount())
                .build();
    }
}
