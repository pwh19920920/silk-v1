package com.spark.bitrade.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.utils.SignUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 *  <p>钱包扫码支付请求dto</p>
 *  @author tian.bo
 *  @date 2019/1/8.
 */
@Data
public class WalletQRPayParameterDTO {

    /**
     * 转入地址
     */
    private String to;

    /**
     * 转出地址
     */
    private String from;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 渠道
     */
    private Integer channel;

    /**
     * 钱包标识ID
     */
    private String walletMarkId;

    /**
     * 昵称
     */
    //private String nickname;

    /**
     * 支付类型
     */
    private Integer payType;

    /**
     * 商家id
     */
    //private String merchantsId;

    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;

    /**
     * 签名
     */
    private String sign;

    /**
     * 时间撮
     */
    private long timestamp;

    private String fromAppId;

    private String toAppId;

    /**
     *
     * @return
     */
    public boolean validateSign(){
        try {
            String json = JSON.toJSONString(this);
            JSONObject jsonObject = JSON.parseObject(json);
            jsonObject.remove("deviceInfo");
            jsonObject.remove("timestamp");
            jsonObject.remove("sign");
            String md5Encode = SignUtil.sign(Long.valueOf(timestamp),jsonObject);
            //判断签名是否合法
            //return md5Encode.equals(sign.toUpperCase());
            return StringUtils.equals(md5Encode,sign);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validation() throws ApiException{
        if(StringUtils.isEmpty(this.getTo())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getFrom())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getSymbol())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(null == this.getAmount() || this.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new ApiException("bad-argument","bad-argument");
        }
        //addl by  shenzucai 时间： 2019.04.03  原因： 兼容区块链钱包和云端分离的情形（不交易钱包markid）
        // if(StringUtils.isEmpty(this.getWalletMarkId())){
        //     throw new ApiException("bad-argument","bad-argument");
        // }
        if(!validateSign()){
            throw new ApiException("api-signature-not-valid","api-signature-not-valid");
        }
        return true;
    }



}
