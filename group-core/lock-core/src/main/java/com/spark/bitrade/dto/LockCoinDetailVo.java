package com.spark.bitrade.dto;

import com.spark.bitrade.constant.SmsSendStatus;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author lingxing
 * @time 2018.08.02 09:13
 */
@Data
public class LockCoinDetailVo {
    //id
    private Long id;
    //手机号码
    private String mobilePhone;
    @Enumerated(EnumType.ORDINAL)
    //短信类型
    private SmsSendStatus smsSendStatus;
}
