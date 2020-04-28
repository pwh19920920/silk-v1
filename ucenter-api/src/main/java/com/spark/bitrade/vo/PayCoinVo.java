package com.spark.bitrade.vo;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CoinFeeType;
import com.spark.bitrade.constant.CommonStatus;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收款方信息
 * @author Zhang Yanjun
 * @time 2019.01.21 13:48
 */
@ApiModel
@Data
public class PayCoinVo {
    /**
     * 缩写
     */
    private String unit;
    /**
     * 状态 0正常
     */
    private CommonStatus status = CommonStatus.NORMAL;

    /**
     * 最小提币手续费
     */
    private double minTxFee;
    /**
     * 对人民币汇率
     */
    private double cnyRate;
    /**
     * 最大提币手续费
     */
    private double maxTxFee;

    /**
     * 是否支持rpc接口 0否
     */
    private BooleanEnum enableRpc = BooleanEnum.IS_FALSE;

    /**
     * 添加主币
     */
    private String baseCoinUnit;

    /**
     * 是否能提币 0否
     */
    private BooleanEnum canWithdraw;

    /**
     * 是否能充币 0否
     */
    private BooleanEnum canRecharge;


    /**
     * 是否能转账 0否
     */
    private BooleanEnum canTransfer = BooleanEnum.IS_TRUE;

    /**
     * 是否能自动提币 0否
     */
    private BooleanEnum canAutoWithdraw;

    /**
     * 提币阈值
     */
    private BigDecimal withdrawThreshold;

    /**
     * 最小提币数量
     */
    private BigDecimal minWithdrawAmount;

    /**
     * 最大提币数量
     */
    private BigDecimal maxWithdrawAmount;

    /**
     * 提币精度
     */
    private int withdrawScale;

    /**
     * 手续费率设置
     */
    private BigDecimal feeRate;

    /**
     * 手续费模式0，固定，1比例
     */
    private CoinFeeType feeType;

    /**
     * 是否具有标签 0正常  1非法
     */
    private CommonStatus hasLabel = CommonStatus.ILLEGAL;

    /**
     * 最小到账金额
     */
    private double minDepositAmount;

    /**
     * 手续费抵扣币种单位（不包括当前币种）
     */

    private String feeDiscountCoinUnit;

    /**
     * 手续费抵扣模式 0固定 1比例
     */
    private CoinFeeType feeDiscountType;

    /**
     * 抵扣数值，手续费抵扣模式为0时，代表具体数量，抵扣模式为1时，代表折扣0.8代表8折
     */
    private BigDecimal feeDiscountAmount;
}
