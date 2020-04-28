package com.spark.bitrade.entity;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.MemberLevelEnum;
import lombok.Data;

import javax.persistence.Column;

/**
 * @author Zhang Jinwei
 * @date 2018年01月31日
 */
@Data
public class LoginInfo {
    private String username;
    private Location location;
    private MemberLevelEnum memberLevel;
    private String token;
    private String accessToken;
    private String realName;
    private Country country;
    private String avatar;
    private String promotionCode;
    private long id;
    private String phone;//手机号
    private String email;
    private BooleanEnum isOpenPhoneLogin = BooleanEnum.IS_FALSE;//是否开启手机认证
    private BooleanEnum isOpenGoogleLogin = BooleanEnum.IS_FALSE;//是否开启google认证
    private BooleanEnum isOpenPhoneUpCoin = BooleanEnum.IS_FALSE;//是否开启手机提币认证
    private BooleanEnum isOpenGoogleUpCoin = BooleanEnum.IS_FALSE;//是否开启google提币认证

}
