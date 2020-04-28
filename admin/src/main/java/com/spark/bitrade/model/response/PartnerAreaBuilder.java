package com.spark.bitrade.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
  * 合伙人查询返回数据
  * @author tansitao
  * @time 2018/5/28 18:16 
  */
@Data
@Builder
public class PartnerAreaBuilder {
    private long partnerId;
    private Long memberId;
    private String realName;
    private String mobilePhone;
    private String userName;
    private PartnerLevle level;
    private String area;
    private PartnerStaus status;

}
