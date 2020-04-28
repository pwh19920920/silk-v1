package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderScreen extends OtcOrderTopScreen{
    private String orderSn;
    private Long advertiseId;//广告id
    private BigDecimal minNumber ;
    private BigDecimal maxNumber ;
    private String memberName;//用户名和真名的关键字即可
    private String customerName;//用户名和真名的关键字即可
    private BigDecimal minMoney;
    private BigDecimal maxMoney;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date releaseStartTime;//放币开始时间
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date releaseEndTime;//放币结束时间

    //add by zyj 2018.11.30 筛选条件增加是否自动取消的判断
    private BooleanEnum isManualCancel;
}
