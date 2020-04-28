package com.spark.bitrade.vo;

import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.08.24 15:52
 */
@Data
@ExcelSheet
public class PartnerAreaVo {

    private long partnerId;
    @Excel(name = "会员ID")
    private Long memberId;
    @Excel(name = "姓名")
    private String realName;
    @Excel(name = "手机号")
    private String mobilePhone;
    @Excel(name = "平台昵称")
    private String userName;
    @Excel(name = "等级")
    private PartnerLevle level;
    @Excel(name = "负责区域")
    private String areaName;

    @Excel(name = "合伙人状态")
    private String partnerStausOut;
    private PartnerStaus partnerStaus;//合伙人状态

}
