package com.spark.bitrade.dto;

import com.spark.bitrade.constant.WalletType;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.exception.RequiredArgException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 条形码生成请求参数
 */
@Data
@ApiModel(value = "PaymentBarCodeGenerate",description = "付款码申请请求对象")
public class PaymentBarCodeGenerateDTO extends SilkpayBaseDTO {

    /**
     * 会员id
     */
    @ApiModelProperty(value = "会员ID", required = true)
    private String accountId;

    /**
     * 钱包类型 云端钱包：1,区块链钱包:2暂不开放区块链钱包)
     */
    @ApiModelProperty(value = "钱包类型 1-云端钱包,2-区块链钱包,暂不开放区块链钱包)", required = true)
    private Integer walletType;

    /**
     * 有效时间，逾期将失效。取值范围：1-10m,m-分钟。（最大10分钟）
     */
    @ApiModelProperty(value = "有效时间，逾期将失效。取值范围：1-10m,m-分钟。（最大10分钟）")
    private String timeoutExpres;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String symbol;

    /**
     * 条码场景被扫：1
     */
    @ApiModelProperty(value = "条码场景,1-被扫",required = true)
    private String scene;


    /**
     * 钱包标识ID
     */
    @ApiModelProperty(value = "钱包标识ID")
    private String walletMarkId;

    /**
     * 参数校验
     * @return
     * @throws RequiredArgException
     */
    public boolean validation() throws RequiredArgException {
        super.validation();
        if(StringUtils.isEmpty(this.getAccountId())){
            throw new RequiredArgException("ISP.MISSING-ACCOUNT-ID","缺少accountId参数");
        }
        if(StringUtils.isEmpty(this.getScene())){
            throw new RequiredArgException("ISP.MISSING-SCENE","缺少scene参数");
        }
        /*if(StringUtils.isEmpty(this.getWalletMarkId())){
            throw new RequiredArgException("ISP.MISSING-WALLET-MARKID","缺少walletMarkId参数");
        }*/
        if(null == walletType){
            throw new RequiredArgException("ISP.MISSING-WALLET-TYPE","缺少walletType参数");
        }
        return true;
    }


}
