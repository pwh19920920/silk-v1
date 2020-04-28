package com.spark.bitrade.dto;

import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.exception.RequiredArgException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * <p>付款码支付请求参数</p>
 * @author tian.bo
 * @since 2019/3/8.
 */
@Data
public class PaymentBarCodePayDTO extends SilkpayBaseDTO{

    /**
     * 会员id
     */
    @ApiModelProperty(value = "会员ID", required = true)
    private String accountId;

    /**
     * 授权码
     */
    @ApiModelProperty(value = "授权码", required = true)
    private String authCode;

    /**
     * 支付场景 条码支付，取值：3
     */
    @ApiModelProperty(value = "支付场景,条码支付,取值：3 ", required = true)
    private String payType;

    /**
     * 扫码场景 扫码枪，取值：1  手机扫一扫,取值：2
     */
    @ApiModelProperty(value = "扫码场景 扫码枪，取值：1  手机扫一扫,取值：2 ", required = true)
    private String scanScene;

    /**
     * 支付币种
     */
    @ApiModelProperty(value = "支付币种")
    private String symbol;

    /**
     * 数量
     */
    @ApiModelProperty(value = "数量")
    private BigDecimal amount;

    /**
     * 渠道
     */
    @ApiModelProperty(value = "渠道")
    private String channel;

    /**
     * 钱包标识ID
     */
    @ApiModelProperty(value = "钱包标识ID")
    private String walletMarkId;

    /**
     * 手续费
     */
    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    /**
     * 参数校验
     * @return
     * @throws RequiredArgException
     */
    public boolean validation() throws RequiredArgException {
        super.validation();
        if(StringUtils.isEmpty(this.getAccountId())){
            throw new ApiException("ISP.MISSING-ACCOUNT-ID","缺少accountId参数");
        }
        if(StringUtils.isEmpty(this.getSymbol())){
            throw new RequiredArgException("ISP.MISSING-SYMBOL","缺少symbol参数");
        }
        if(StringUtils.isEmpty(this.getScanScene())){
            throw new RequiredArgException("ISP.MISSING-SCAN-SCENE","缺少scanScene参数");
        }
        if(StringUtils.isEmpty(this.getPayType())){
            throw new RequiredArgException("ISP.MISSING-PAY-TYPE","缺少payType参数");
        }
        if(StringUtils.isEmpty(this.getAuthCode())){
            throw new RequiredArgException("ISP.MISSING-AUTH-CODE","缺少authCode参数");
        }
     /*   if(StringUtils.isEmpty(this.getChannel())){
            throw new RequiredArgException("ISP.MISSING-CHANNEL","缺少channel参数");
        }*/
       /* if(StringUtils.isEmpty(this.getWalletMarkId())){
            throw new RequiredArgException("ISP.MISSING-WALLET-MARKID","缺少walletMarkId参数");
        }*/
        if(null == this.getAmount()){
            throw new RequiredArgException("ISP.MISSING-AMOUNT","缺少amount参数");
        }
       /* if(null == this.getFee()){
            throw new RequiredArgException("ISP.MISSING-FEE","缺少fee参数");
        }*/
        return true;
    }


}
