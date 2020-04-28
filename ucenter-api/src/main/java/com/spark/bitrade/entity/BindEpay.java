package com.spark.bitrade.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
  * 绑定epay
  * @author tansitao
  * @time 2018/8/16 10:22 
  */
@Data
public class BindEpay {
    @NotBlank(message = "{BindEpay.realName.null}")
    private String realName;
    @NotBlank(message = "{BindEpay.ePay.null}")
    private String epayNo;
    @NotBlank(message = "{BindEpay.jyPassword.null}")
    private String jyPassword;
}
