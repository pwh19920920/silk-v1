package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;

/**
  * 用户安全权限
  * @author tansitao
  * @time 2018/7/5 9:17 
  */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberSecuritySet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @Column(columnDefinition = "bigint(20) comment '会员id'")
    private long memberId;

    @Column(columnDefinition = "varchar(8) DEFAULT '0' comment '是否开启手机登录认证'")
    private BooleanEnum isOpenPhoneLogin = BooleanEnum.IS_FALSE;

    @Column(columnDefinition = "varchar(8) DEFAULT '0' comment '是否开启google登录认证'")
    private BooleanEnum isOpenGoogleLogin = BooleanEnum.IS_FALSE;

    @Column(columnDefinition = "varchar(8) DEFAULT '0' comment '是否开启手机提币认证'")
    private BooleanEnum isOpenPhoneUpCoin = BooleanEnum.IS_FALSE;

    @Column(columnDefinition = "varchar(8) DEFAULT '0' comment '是否开启google提币认证'")
    private BooleanEnum isOpenGoogleUpCoin = BooleanEnum.IS_FALSE;

    @Column(columnDefinition = "varchar(8) DEFAULT '0' comment '是否开启总资产显示'")
    private BooleanEnum isOpenPropertyShow = BooleanEnum.IS_FALSE;

    //add by qhliao

    @Column(columnDefinition = "varchar(8) DEFAULT '1' comment '是否开启场外买入交易 0关闭 1开启'")
    private BooleanEnum isOpenExPitTransaction =BooleanEnum.IS_TRUE;
    @Column(columnDefinition = "varchar(8) DEFAULT '1' comment '是否开启场外卖出交易 0关闭 1开启'")
    private BooleanEnum isOpenExPitSellTransaction =BooleanEnum.IS_TRUE;
    @Column(columnDefinition = "varchar(8) DEFAULT '1' comment '是否开启BB交易 0关闭 1开启'")
    private BooleanEnum isOpenBbTransaction =BooleanEnum.IS_TRUE;
    @Column(columnDefinition = "varchar(8) DEFAULT '1' comment '是否开启提币 0关闭 1开启'")
    private BooleanEnum isOpenUpCoinTransaction =BooleanEnum.IS_TRUE;
    @Column(columnDefinition = "varchar(8) DEFAULT '1' comment '是否开启平台内部转账 0关闭 1开启'")
    private BooleanEnum isOpenPlatformTransaction =BooleanEnum.IS_TRUE;

}
