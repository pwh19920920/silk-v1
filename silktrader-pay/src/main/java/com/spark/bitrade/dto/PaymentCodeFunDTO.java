package com.spark.bitrade.dto;

import lombok.Data;

/**
 * <p>扫码付功能开启/查询dto</p>
 * @author tian.bo
 * @date 2019/3/6.
 */
@Data
public class PaymentCodeFunDTO extends SilkpayBaseDTO {

    private String accoutId;

    private String cmd;

    private String terminalDeviceInfo;

    private Integer strategyType;





}
