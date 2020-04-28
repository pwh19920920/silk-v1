package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.WithdrawStatus;
import lombok.Data;

import java.util.Date;

/**
 * @author shenzucai
 * @time 2018.06.13 17:09
 */
@Data
public class WithdrawFeeStatScreen {

    /**
     * 交易时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    private String unit ;
}
