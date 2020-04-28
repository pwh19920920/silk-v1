package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
  * 归集日志查询
  * @author tansitao
  * @time 2018/5/12 9:47 
  */
@Data
public class CollectScreen extends AccountScreen{

    /**
     * 归集时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    private String symbol;

}
