package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
  * 平台币种
  * @author tansitao
  * @time 2018/5/12 9:47 
  */
@Data
public class FincPlatStatScreen extends AccountScreen{

    /**
     * 归集时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    private String symbol;

}
