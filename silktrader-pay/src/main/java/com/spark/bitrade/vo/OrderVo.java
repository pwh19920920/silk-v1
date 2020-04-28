package com.spark.bitrade.vo;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.CoinFeeType;
import com.spark.bitrade.constant.PayTransferStatus;
import com.spark.bitrade.constant.PayTransferType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author shenzucai
 * @time 2019.08.27 11:25
 */
@Data
public class OrderVo {

    //流水id
    private Long id;
    //交易编号
    private String tradeSn;
    //收款地址
    private String receiptAddress;
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

    //转账时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //状态（发起、成功、失败）
    private PayTransferStatus status;
}
