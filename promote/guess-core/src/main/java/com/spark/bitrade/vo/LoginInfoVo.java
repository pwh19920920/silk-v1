package com.spark.bitrade.vo;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.entity.Country;
import com.spark.bitrade.entity.Location;
import lombok.Data;

/**
  * 登录信息
  * @author tansitao
  * @time 2018/9/25 22:08 
  */
@Data
public class LoginInfoVo {
    private String username;
    private Location location;
    private MemberLevelEnum memberLevel;
    private String token;
    private String realName;
    private Country country;
    private String avatar;
    private String promotionCode;
    private long id;
    private String phone;//手机号
    private BooleanEnum isOpenPhoneLogin = BooleanEnum.IS_FALSE;//是否开启手机认证
    private BooleanEnum isOpenGoogleLogin = BooleanEnum.IS_FALSE;//是否开启google认证
    private BooleanEnum isOpenPhoneUpCoin = BooleanEnum.IS_FALSE;//是否开启手机提币认证
    private BooleanEnum isOpenGoogleUpCoin = BooleanEnum.IS_FALSE;//是否开启google提币认证

}
