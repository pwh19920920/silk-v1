package com.spark.bitrade.entity;

import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Builder;
import lombok.Data;

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
