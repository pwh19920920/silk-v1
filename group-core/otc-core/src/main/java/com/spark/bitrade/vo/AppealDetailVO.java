package com.spark.bitrade.vo;

import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.AppealType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.entity.OrderAppealAccessory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 申诉详情vo
 * @author Zhang Yanjun
 * @time 2018.10.29 15:39
 */
@ApiModel
@Data
public class AppealDetailVO {
    /**
     * 申诉单id
     */
    @ApiModelProperty(value = "申诉单id",name = "appealId")
    private BigInteger appealId ; //edit by yangch 时间： 2018.04.29 原因：合并

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号",name = "orderSn",example = "79866614866120704")
    private String orderSn ;

    /**
     * 广告创建者
     */
    @ApiModelProperty(value = "广告创建者id",name = "advertiseCreatrId",example = "74737")
    private String advertiseCreaterId ;
    @ApiModelProperty(value = "广告创建者用户名",name = "advertiseCreaterUserName",example = "张珊")
    private String advertiseCreaterUserName ;

    @ApiModelProperty(value = "广告创建者真实姓名",name = "advertiseCreaterName",example = "张珊")
    private String advertiseCreaterName ;

    @ApiModelProperty(value = "广告创建者手机号",name = "advertiseCreaterPhone",example = "15923525624")
    private String advertiseCreaterPhone ;

    @ApiModelProperty(value = "广告创建者邮箱",name = "advertiseCreaterEmail",example = "15923525624@163.com")
    private String advertiseCreaterEmail ;

    @ApiModelProperty(value = "广告创建者角色",name = "advertiseCreaterRole",example = "买比方、申诉方、胜方")
    private String advertiseCreaterRole ;

    /**
     * 交易者
     */
    @ApiModelProperty(value = "交易方id",name = "customerId",example = "74737")
    private String customerId ;

    @ApiModelProperty(value = "交易方用户名",name = "customerUserName",example = "君")
    private String customerUserName ;

    @ApiModelProperty(value = "交易方真实姓名",name = "customerRealName",example = "张三")
    private String customerRealName ;

    @ApiModelProperty(value = "交易方手机号",name = "customerPhone",example = "13562605826")
    private String customerPhone ;

    @ApiModelProperty(value = "交易方邮箱",name = "customerEmail",example = "13562605826@163.com")
    private String customerEmail ;

    @ApiModelProperty(value = "交易方角色",name = "customerRole",example = "买比方、申诉方、胜方")
    private String customerRole;

    @ApiModelProperty(value = "申诉方id",name = "initiatorId",example = "74737")
    private String initiatorId;

    @ApiModelProperty(value = "是否胜诉,0:否 1：是",name = "isSuccess")
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isSuccess ;

    @ApiModelProperty(value = "订单创建时间",name = "orderCreateTime")
    private Date orderCreateTime;

    @ApiModelProperty(value = "订单类型，0:买入 1:卖出",name = "advertiseType")
    private int advertiseType;

    @ApiModelProperty(value = "付款方式",name = "payMode")
    private String payMode;

    @ApiModelProperty(value = "成交价格(单价)",name = "price")
    private BigDecimal price;

    @ApiModelProperty(value = "订单金额",name = "money",example = "100")
    private BigDecimal money ;

    @ApiModelProperty(value = "订单数量",name = "number",example = "10")
    private BigDecimal number ;

    @ApiModelProperty(value = "币种名称",name = "unit",example = "USDT")
    private String unit ;

    @ApiModelProperty(value = "场外订单申诉附件表",name = "list")
    List<OrderAppealAccessory> list;

    //订单付款时间
    @ApiModelProperty(value = "订单付款时间",name = "payTime")
    private Date payTime;
    //订单申诉时间
    @ApiModelProperty(value = "订单申诉时间",name = "appealCreateTime")
    private Date appealCreateTime;
    //申诉处理时间
    @ApiModelProperty(value = "订单处理时间",name = "dealWithTime")
    private Date dealWithTime;
    @ApiModelProperty(value = "申诉取消方id",name = "cancelId",example = "74737")
    private String cancelId;
    //申诉取消时间
    @ApiModelProperty(value = "申诉取消时间",name = "appealCancelTime")
    private Date appealCancelTime;
    //申诉取消原因
    @ApiModelProperty(value = "申诉取消原因 0已经联系上卖家，等待卖家放币，1卖家已确认到账，等待卖家放币，2买家已付款，3其他'",name = "cancelReason")
    private Integer cancelReason;
    //申诉取消原因描述
    @ApiModelProperty(value = "申诉取消原因描述",name = "cancelReasonDescription")
    private String cancelReasonDescription;
    //订单取消时间
    @ApiModelProperty(value = "订单取消时间",name = "orderCancelTime")
    private Date orderCancelTime;
    @ApiModelProperty(value = "订单关闭时间",name = "orderCloseTime")
    private Date orderCloseTime;
    //订单状态
    private OrderStatus orderStatus;
    //申诉是否处理
    @Enumerated(EnumType.ORDINAL)
    private AppealStatus status ;

    //申诉类型  0请求放币，1请求取消订单，2其他
    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(value = "申诉类型 0请求放币，1请求取消订单，2其他",name = "appealType")
    private AppealType appealType;

//    //add by zyj 2018.11.1
//    @ApiModelProperty(value = "胜诉缘由",name = "successRemark")
//    private String successRemark;

    @ApiModelProperty(value = "申诉缘由",name = "remark")
    private String remark;

}
