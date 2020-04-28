package com.spark.bitrade.entity;

import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Data;

/**
  * 第三方预登陆，需要提供的信息
  * @author tansitao
  * @time 2018/10/9 15:27 
  */
@Data
public class ThirdLogin {

    private String phone;  //手机号

    private String email; //邮箱

    private String country; //国家

    private String promotion; //推荐码

    private MemberLevelEnum memberLevel; //用户等级

    private String realName; //会员真实姓名

    private String idNumber; //身份证号码





}
