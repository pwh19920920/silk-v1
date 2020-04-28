package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author shenzucai
 * @time 2018.06.13 17:09
 */
@Data
public class ExchangeFeeStatScreen {

    /**
     * 交易时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;


    /**
     * 交易币
     */
    private String coinUnit ;

    /**
     * 定价币
     */
    private String baseUnit;
}
