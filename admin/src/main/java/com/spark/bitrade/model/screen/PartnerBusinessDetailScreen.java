package com.spark.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
  * 合伙人业务月收益详细条件
  * @author tansitao
  * @time 2018/5/29 15:32 
  */
@Data
public class PartnerBusinessDetailScreen extends AccountScreen{


    private long memberId;
    /**
     * 查询开始时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    /**
     * 查询结束时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

}
