package com.spark.bitrade.dto;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.constant.RealNameStatus;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.11.01 13:58
 */
@Data
public class MemberSecurityInfoDto {

    private Long id;

    private String weChat;

    private String bank;

    private String aliNo;

    private String epayNo;

    private String jyPassword;

    private String phone;

    private String email;

    private RealNameStatus realNameStatus;

    private CertifiedBusinessStatus certifiedBusinessStatus;

    private BooleanEnum isOpenGoogleLogin;

    private BooleanEnum isOpenGoogleUpCoin;

    private BooleanEnum isOpenPhoneLogin;

    private BooleanEnum isOpenPhoneUpCoin;

    private BooleanEnum isOpenPropertyShow;


}
