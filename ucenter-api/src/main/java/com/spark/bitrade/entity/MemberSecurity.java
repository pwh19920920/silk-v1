package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年01月15日
 */
@Builder
@Data
public class MemberSecurity {
    private String username;
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private BooleanEnum realVerified;
    private BooleanEnum emailVerified;
    private BooleanEnum phoneVerified;
    private BooleanEnum loginVerified;
    private BooleanEnum fundsVerified;
    private BooleanEnum realAuditing;
    private BooleanEnum certBusiness;
    private Country country;
    private String mobilePhone;
    private String email;
    private String realName;
    private String realNameRejectReason;
    private String idCard;
    private String avatar;
    private String googleKey;
    private int googleState;
    private BooleanEnum businessVerified; //edit by yangch 2018-04-14 是否商家认证
    private BooleanEnum accountVerified; //add by yangch 时间： 2018.05.03 原因：代码合并
    private Global global; //add by tansitao 时间： 2018/5/11 原因：全局变量配置
    private BooleanEnum partnerVerified; ////add by tansitao 时间： 2018/5/31 原因：增加是否为合伙人身份认证
    private BooleanEnum isDeakingMember; ////add by tansitao 时间： 2018/5/31 原因：增加是否为迪肯员工
    private BooleanEnum isOpenPhoneLogin;//是否开启手机认证
    private BooleanEnum isOpenGoogleLogin;//是否开启google认证
    private BooleanEnum isOpenPhoneUpCoin;//是否开启手机提币认证
    private BooleanEnum isOpenGoogleUpCoin;//是否开启google提币认证
    private BooleanEnum isOpenPropertyShow;//是否开启总资产显示
    private int transactions;//交易次数
    private BooleanEnum isBindBank;//是否绑定银行卡
    private BooleanEnum isBindAliPay;//是否绑定支付宝
    private BooleanEnum isBindWechatPay;//是否绑定微信
    private BooleanEnum isBindEpay;//是否绑定易派
}
