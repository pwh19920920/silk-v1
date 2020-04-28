package com.spark.bitrade.entity;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Zhang Jinwei
 * @date 2018年01月20日
 */
@Builder
@Data
public class OrderDetail {
    private String orderSn;
    private AdvertiseType type;
    private String unit;
    private OrderStatus status;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal amount;
    private BigDecimal commission;
//    private PayInfo payInfo;
    private JSONObject payInfo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Date payTime;
    private int timeLimit;
    private String otherSide;
    private long myId;
    private long hisId;
    private String payCode;
    private String remark; //add by yangch 时间： 2018.05.05 原因：顾客需求
    private Country country; //add by tansitao 时间： 2018/8/15 原因：国家
    private String otherPhone; ////add by tansitao 时间： 2018/9/4 原因：对方手机号
    private BooleanEnum isManualCancel;//add by tansitao 时间： 2018/9/4  原因：添加是否为手动取消订单，（1：表示手动取消订单，0：表示不是手动取消订单）
    private int traderNum;//add by tansitao 时间： 2018/9/4 原因：商家交易笔数
    private AppealStatus appealStatus; //add by tansitao 时间： 2018/9/4 原因：订单申诉状态
    private PayMode payMode;//add by tansitao 时间： 2018/9/4 原因：支付方式
    private String payKey;
    private JSONObject payModeInfo; // add by wsy 时间：2019-5-27 18:58:15 原因：支付方式账号信息
    private String businessNickname;//add by tansitao 时间： 2018/9/5 原因：商家昵称

    //=======1.3需求 开始==========
//    private long appelerId; //add by tansitao 时间： 2018/11/1 原因：申诉者id
    private String otherEmail; //add by tansitao 时间： 2018/11/13 原因：对方邮箱
    private List<OrderAppealAccessory> orderAppealAccessories; //add by tansitao 时间： 2018/11/1 原因：申诉材料
    private List<AdminOrderAppealSuccessAccessory> orderAppealSuccessAccessories; //add by tansitao 时间： 2018/11/1 原因：申诉结果材料
    private Appeal appeal; //add by tansitao 时间： 2018/11/1 原因：申诉记录
    //add by ss 时间2020/03/28 原因：获取订单支持的交易方式
    private String supportPayModes;
//    private BooleanEnum isSupportBank = BooleanEnum.IS_FALSE; //add by tansitao 时间： 2018/11/10 原因：是否支持银行卡支付
//    private BooleanEnum isSupportAliPay = BooleanEnum.IS_FALSE;//add by tansitao 时间： 2018/11/10 原因：是否支持支付宝支付
//    private BooleanEnum isSupportWechatPay = BooleanEnum.IS_FALSE;//add by tansitao 时间： 2018/11/10 原因：是否支持微信支付
//    private BooleanEnum isSupportEPay = BooleanEnum.IS_FALSE;//add by tansitao 时间： 2018/11/10 原因：是否支持易派支付
    //=======1.3需求  结束=========

    private BigDecimal orderMoney; // 订单金额
    private BigDecimal serviceRate; // 服务费率
    private BigDecimal serviceMoney; // 服务费
    private AdvertiseType advertiseType; // 广告类型
    private BooleanEnum businessVerified; // 是否为认证商家
    private BooleanEnum isBusiness; // 是否为商家
    /**
     * 法币ID
     */
    private Long currencyId;

    /**
     * 法币名称
     */
    private String currencyName;

    /**
     * 法币单位
     */
    private String currencyUnit;

    /**
     * 法币符号
     */
    private String currencySymbol;
}
