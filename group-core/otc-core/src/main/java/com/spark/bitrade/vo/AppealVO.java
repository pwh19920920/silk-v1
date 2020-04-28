package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.AppealStatus;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.entity.Appeal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@ApiModel
@Data
public class AppealVO {

    /**
     * 申诉单id
     */
    @ApiModelProperty(value = "申诉单id",name = "appealId")
    private BigInteger appealId ; //edit by yangch 时间： 2018.04.29 原因：合并

    /**
     * 广告创建者
     */
    @ApiModelProperty(value = "广告创建者用户名",name = "advertiseCreaterUserName",example = "张珊")
    private String advertiseCreaterUserName ;

    @ApiModelProperty(value = "广告创建者真实姓名",name = "advertiseCreaterName",example = "张珊")
    private String advertiseCreaterName ;

    /**
     *
     */
    @ApiModelProperty(value = "客户用户名",name = "customerUserName",example = "李思")
    private String customerUserName ;

    @ApiModelProperty(value = "客户真实姓名",name = "customerName",example = "李思")
    private String customerName ;

    /**
     * 被投诉者真名
     */
    @ApiModelProperty(value = "被投诉者真名",name = "associateName",example = "李思")
    private String associateName ;
    /**
     * 被投诉者用户名
     */
    @ApiModelProperty(value = "被投诉者用户名",name = "associateUsername",example = "李思")
    private String associateUsername ;
    /**
     * 申诉者用户名
     */
    @ApiModelProperty(value = "申诉者用户名",name = "initiatorUsername",example = "李思")
    private String initiatorUsername ;
    /**
     * 申诉者真名
     */
    @ApiModelProperty(value = "申诉者真名",name = "initiatorName",example = "李思")
    private String initiatorName ;

    /**
     * 订单手续费
     */
    @ApiModelProperty(value = "订单手续费",name = "fee",example = "0.001")
    private BigDecimal fee ;

    /**
     * 订单数量
     */
    @ApiModelProperty(value = "订单数量",name = "number",example = "10")
    private BigDecimal number ;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额",name = "money",example = "100")
    private BigDecimal money ;

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号",name = "orderSn",example = "79866614866120704")
    private String orderSn ;

    /**
     * 交易时间
     */
    @ApiModelProperty(value = "交易时间",name = "transactionTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date transactionTime;

    /**
     * 投诉时间
     */
    @ApiModelProperty(value = "投诉时间",name = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 处理时间
     */
    @ApiModelProperty(value = "投诉时间",hidden = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;

    /**
     * 处理时间
     */
    @ApiModelProperty(value = "处理时间",name = "dealWithTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealWithTime;

    /**
     * 付款方式
     */
    @ApiModelProperty(value = "付款方式",name = "payMode")
    private String payMode;

    /**
     * 币种名称
     */
    @ApiModelProperty(value = "币种名称",name = "coinName")
    private String coinName ;

    /**
     * 订单状态
     */
 /*   @Enumerated(EnumType.STRING)*///edit by yangch 时间： 2018.04.29 原因：合并
    @ApiModelProperty(value = "订单状态，0:已取消 1:未付款 2:已付款 3:已完成 4:申诉中 5:已关闭",name = "orderStatus")
    private int orderStatus ;

    /**
     * 是否胜诉
     */
    /*@Enumerated(EnumType.STRING)*///edit by yangch 时间： 2018.04.29 原因：合并
    @ApiModelProperty(value = "是否胜诉,0:否 1：是",name = "isSuccess")
    private int isSuccess;

    /**
     * 订单类型
     */
    /*@Enumerated(EnumType.STRING)*/ //edit by yangch 时间： 2018.04.29 原因：合并
    @ApiModelProperty(value = "订单类型，0:买入 1:卖出",name = "advertiseType")
    private int advertiseType;

    @ApiModelProperty(value = "处理状态，0:未处理 1:已处理 2:已取消",name = "status")
    private int status; //edit by yangch 时间： 2018.04.29 原因：合并

    @ApiModelProperty(value = "申诉原因",name = "remark")
    private String remark;

    @ApiModelProperty(value = "客户电话",name = "remark")
    private String customerPhone;

    @ApiModelProperty(value = "客户邮箱",name = "remark")
    private String customerEmail;

    @ApiModelProperty(value = "被投诉者电话",name = "remark")
    private String associatePhone;

    @ApiModelProperty(value = "被投诉者邮箱",name = "remark")
    private String associateEmail;

    @ApiModelProperty(value = "成交价格",name = "remark")
    private BigDecimal price;

    @ApiModelProperty(value = "订单创建时间",name = "orderCreateTime")
    private Date orderCreateTime;
}
