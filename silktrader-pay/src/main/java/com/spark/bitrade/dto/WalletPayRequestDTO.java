package com.spark.bitrade.dto;

import com.spark.bitrade.exception.ApiException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>钱包支付请求参数</p>
 * @author tian.bo
 * @date 2019/1/9.
 */
@Data
public class WalletPayRequestDTO {

    /**
     * 时间戳
     */
    private String tt;
    /**
     * 业务参数(加密后的)
     */
    private String ct;
    /**
     * 加密盐 16位
     */
    private String sl;
    /**
     * 向量 16位
     */
    private String vt;
    /**
     * 签名
     */
    private String sg;


    /**
     * 参数校验
     * @return
     * @throws ApiException
     */
    public boolean validation() throws ApiException{
        if(StringUtils.isEmpty(this.getTt())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getSl())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getVt())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getCt())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getSg())){
            throw new ApiException("bad-argument","bad-argument");
        }
        return true;
    }


}
