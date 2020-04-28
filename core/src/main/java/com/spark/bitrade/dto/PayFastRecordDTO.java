package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.CoinFeeType;
import com.spark.bitrade.constant.PayTransferStatus;
import com.spark.bitrade.constant.PayTransferType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 快速支付记录表
 * （记录基于星客账户之间的支付记录）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@Data
public class PayFastRecordDTO {
    //流水id
    private Long id;
    //交易编号
    private String tradeSn;
    //收款用户ID
    private Long receiptId;
    //收款账户id（memberWallet表）
    private Long receiptWalletId;
    //收款方手机号
    private String receiptPhone;
    //收款用户角色id
    private Long receiptRoleId;
    //收款用户角色(冗余)
    private String receiptRole;
    //收款地址
    private String receiptAddress;
    //支付用户ID
    private Long payId;
    //支付账户id（memberWallet表）
    private Long payWalletId;
    //支付方手机号
    private String payPhone;
    //支付用户角色id
    private Long payRoleId;
    //支付用户角色(冗余)
    private String payRole;
    //支付地址
    private String payAddress;
    //支付金额
    private BigDecimal payMoney;
    //支付币数量
    private BigDecimal amount;
    //实际到账数量
    private BigDecimal arrivedAmount;
    //币种
    private String unit;
    //手续费币种
    private String feeUnit;

    //手续费方式(固定，比例)
    private CoinFeeType feeType;
    //手续费
    private BigDecimal fee;
    //优惠手续费
    private BigDecimal discountsFee;
    //交易类型
    private PayTransferType tradeType;
    //转账方应用ID
    private String platform;
    //收款方应用ID
    private String platformTo ;
    //转账时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //状态（发起、成功、失败）
    private PayTransferStatus status;
    //备注
    private String comment;
    // 标志
    private String tag;
}
