package com.spark.bitrade.dto;

import com.spark.bitrade.exception.ApiException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;



@Data
public class NotifyWalletQRPayParameterDTO extends WalletQRPayParameterDTO{
    private String tag;

    private String orderId;

    @Override
    public boolean validation() throws ApiException {
        super.validation();
        if(StringUtils.isEmpty(this.getTag())){
            throw new ApiException("bad-argument","bad-argument");
        }
        if(StringUtils.isEmpty(this.getOrderId())){
            throw new ApiException("bad-argument","bad-argument");
        }
        return true;
    }
}
