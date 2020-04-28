package com.spark.bitrade.dto;

import com.spark.bitrade.util.MD5Util;
import com.spark.bitrade.util.UUIDUtil;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fumy
 * @time 2018.10.22 15:22
 */
@Data
public class PayApplyDTO {

    /**
     * 商户silkTrader平台账号
     */
    private String busiAccount;

    /**
     * 法币类型
     */
    private String currency;

    /**
     * 签约币种
     */
    private String contractCoin;

    /**
     * 入账币种
     */
    private String busiCoin;

    /**
     * 手续费率
     */
    private BigDecimal busiCoinFeeRate;

    /**
     * 异步通知url
     */
    private String asyncNotifyUrl;

    /**
     * 签约周期
     */
    private int period;

    /**
     * 订单超时时间
     */
    private Long expireTime;
}
