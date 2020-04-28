package com.spark.bitrade.dto;

import com.spark.bitrade.exception.RequiredArgException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>silkpay</p>
 * @author tian.bo
 * @date 2019/3/6.
 */
@Data
public class SilkpayBaseDTO {

    public String pid;
    public String appId;
    public String token;
    public boolean validation() throws RequiredArgException {
        if(StringUtils.isEmpty(this.getPid())){
            throw new RequiredArgException("ISP.MISSING-PID","缺少pid参数");
        }
        if(StringUtils.isEmpty(this.getAppId())){
            throw new RequiredArgException("ISP.MISSING-APP-ID","缺少appId参数");
        }
        /*if(StringUtils.isEmpty(this.getToken())){
            throw new RequiredArgException("ISP.MISSING-TOKEN","缺少TOKEN参数");
        }*/
        return true;
    }


}
