package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Zhang Yanjun
 * @time 2018.08.20 14:55
 */
@Data
@ExcelSheet
public class MemberVO implements Serializable {

    @Excel(name = "会员id")
    private long id;
    @Excel(name = "会员名称")
    private String username;
    @Excel(name = "邮箱")
    private String email;
    @Excel(name="手机号")
    private String mobilePhone ;

    @Excel(name = "会员等级")
    private String ml;
    private MemberLevelEnum memberLevel;//会员等级

    @Excel(name = "真实姓名")
    private String realName;

    @Excel(name = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String registrationTime;

    @Excel(name = "交易状态")
    private String ts;
    private BooleanEnum transactionStatus;//交易状态

    @Excel(name = "会员状态")
    private String cs;
    private CommonStatus status;//会员状态
}
