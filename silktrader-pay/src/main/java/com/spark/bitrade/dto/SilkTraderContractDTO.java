package com.spark.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.ContractEnum;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.07.09 09:20
 */
@Data
public class SilkTraderContractDTO {

    /**
     * 签约编号
     */
    private String contractNo;
    /**
     * 商户账号
     */
    private String busiAccount;
    /**
     * 通讯秘钥
     */
    private String messageKey;

    /**
     * 折扣率
     */
    private BigDecimal discount;
    /**
     * 启用状态
     */
    @Enumerated(EnumType.ORDINAL)
    private ContractEnum status = ContractEnum.IS_FALSE;


    /**
     * 异步通知url
     */
    private String asyncNotifyUrl;

    /**
     * 签约起始时间（yyyy-MM-dd HH:mm:ss）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date contractStart;


    /**
     * 签约周期
     */

    private Long period;

    /**
     * 订单超时时间
     */
    private Long expireTime;


    /**
     * 法币类型
     */
    private String currency;

    /**
     * 商家签约币种
     */
    private String contractCoin;

    /**
     * 商家入账币种
     */
    private String busiCoin;

    /**
     * 手续费率
     */
    private BigDecimal busiCoinFeeRate;
    /**
     * 签约细节id
     */
    private Long id;

    /**
     * 商家主页访问地址
     */
    private String busiUrl;
}
