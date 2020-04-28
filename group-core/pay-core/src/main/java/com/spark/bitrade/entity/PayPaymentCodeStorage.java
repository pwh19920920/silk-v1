package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.spark.bitrade.constant.WalletType;
import lombok.Data;

import java.util.Date;

/**
 * Created by Administrator on 2019/3/8.
 */
@Data
@TableName("pay_payment_code_storage")
public class PayPaymentCodeStorage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流水号
     */
    private String tradeNo;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 企业id
     */
    private String pid;

    /**
     * 应用id
     */
    private String appId;
    private String barCode;
    private String authCode;
    private Date timeoutExpres;
    private Date createTime;
    private String symbol;
    private Integer walletType;
    private String walletMarkId;

    /**
     * 场景(1-被扫)
     */
    private Integer scene;

    /**
     * 付款码支付状态
     */
    private Integer status;

}
