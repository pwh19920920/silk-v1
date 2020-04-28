package com.spark.bitrade.vo;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CertifiedBusinessStatus;
import com.spark.bitrade.constant.RealNameStatus;
import com.spark.bitrade.entity.PayStatusInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户安全信息设置vo类
 * @author fumy
 * @time 2018.11.01 11:30
 */
@ApiModel
@Data
public class MemberSecurityInfoVo {

    @ApiModelProperty(value = "会员id",name = "memberId",dataType = "Long")
    private Long memberId;

    private PayStatusInfo payment;

    @ApiModelProperty(value = "资金密码,0：否 1：是",name = "jyPassword",dataType = "Enum")
    private BooleanEnum jyPassword=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "手机,0：否 1：是",name = "phone",dataType = "Enum")
    private BooleanEnum phone=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "邮箱地址,0：否 1：是",name = "email",dataType = "Enum")
    private BooleanEnum email=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "实名认证状态,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum realNameStatus=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "商家认证状态,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum certifiedBusinessStatus=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "是否开启谷歌登录,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum isOpenGoogleLogin=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "是否开启谷歌提币,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum isOpenGoogleUpCoin=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "是否开启手机登录,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum isOpenPhoneLogin=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "是否开启手机提币,0：否 1：是",name = "memberId",dataType = "Enum")
    private BooleanEnum isOpenPhoneUpCoin=BooleanEnum.IS_FALSE;

    @ApiModelProperty(value = "是否显示资产,0：否 1：是",name = "isOpenPropertyShow",dataType = "Enum")
    private BooleanEnum isOpenPropertyShow=BooleanEnum.IS_FALSE;

}
