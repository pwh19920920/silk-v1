package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 三方平台支付商户申请表
 * @author fumy
 * @time 2018.08.03 11:13
 */
@ApiModel
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdPlatformApply {

    @ApiModelProperty(value = "id",name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 商户silkTrader平台账号
     */
    @ApiModelProperty(value = "商户silkTrader平台账号",name = "busiAccount")
    private String busiAccount;

    /**
     * 法币类型
     */
    @ApiModelProperty(value = "法币类型,ex:CNY",name = "currency")
    private String currency;

    /**
     * 签约币种
     */
    @ApiModelProperty(value = "签约币种",name = "contractCoin")
    private String contractCoin;

    /**
     * 入账币种
     */
    @ApiModelProperty(value = "入账币种",name = "busiCoin")
    private String busiCoin;

    /**
     * 手续费率
     */
    @ApiModelProperty(value = "手续费率",name = "busiCoinFeeRate")
    private BigDecimal busiCoinFeeRate;

    /**
     * 异步通知url
     */
    @ApiModelProperty(value = "异步通知url",name = "asyncNotifyUrl")
    private String asyncNotifyUrl;

    /**
     * 签约周期
     */
    @ApiModelProperty(value = "签约周期,天数",name = "period")
    private int period;

    /**
     * 订单超时时间
     */
    @ApiModelProperty(value = "订单超时时间，-1不超时",name = "expireTime")
    private Long expireTime;

    /**
     * 备注（审核不通过时需要填写）
     */
    @ApiModelProperty(value = "备注",name = "comment")
    private String comment;

    /**
     *  申请时间
     */
    @ApiModelProperty(value = "申请时间",name = "applyTime")
    private Date applyTime;

    /**
     * 审核状态，0:待审核，1：审核未通过，2：审核通过
     */
    @ApiModelProperty(value = "审核状态，0:待审核，1：审核未通过，2：审核通过",name = "status")
    private int status=0;

    /**
     * 申请key
     */
    @ApiModelProperty(value = "申请key",name = "applyKey")
    private String applyKey;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间",name = "lastTime")
    private Date lastTime;

}
