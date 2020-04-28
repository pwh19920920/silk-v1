package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author shenzucai
 * @time 2018.06.13 17:09
 */
@Data
public class TechRechargeRecordScreen {

    /**
     * 交易时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
    /**
     * 币种
     */
    private String coinUnit ;
    /**
     * 用户账号
     */
    private String memberAccount;

    private BigDecimal rechargeNumber;
}
