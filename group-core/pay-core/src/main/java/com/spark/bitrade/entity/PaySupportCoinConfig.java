package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包支付币种配置表
 * （用于管理钱包客户端上支持支付的币种配置）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@Data
@ApiModel
@TableName("pay_support_coin_config")
public class PaySupportCoinConfig {
    //id
    @ApiModelProperty(value = "id",name = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    //币种
    @ApiModelProperty(value = "币种",name = "unit")
    private String unit;

    //合约地址
    @ApiModelProperty(value = "合约地址",name = "address")
    private String address;

    //状态（0无效，1有效）
    @ApiModelProperty(value = "状态（0无效，1有效）",name = "status")
    private BooleanEnum status;

    //创建时间
    @ApiModelProperty(value = "创建时间",name = "createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //更新时间
    @ApiModelProperty(value = "更新时间",name = "updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    //是否支持快速转账(0否，1 是)
    @ApiModelProperty(value = "是否支持快速转账(0否，1 是)",name = "isRapidTransfer")
    private BooleanEnum isRapidTransfer;

    //是否支持资产划转(0否，1 是)
    @ApiModelProperty(value = "是否支持资产划转(0否，1 是)",name = "isAssetTransfer")
    private BooleanEnum isAssetTransfer;

    //是否支持资产快速划转（0否，1 是）
    @ApiModelProperty(value = "是否支持资产快速划转（0否，1 是）",name = "isRapidAssetTransfer")
    private BooleanEnum isRapidAssetTransfer;

    //资产正常划转手续费
    @ApiModelProperty(value = "资产正常划转手续费",name = "assetTransferFee")
    private BigDecimal assetTransferFee;

    //资产快速划转手续费
    @ApiModelProperty(value = "资产快速转账手续费",name = "assetTransferRapidFee")
    private BigDecimal assetTransferRapidFee;

    //是否在Silubium钱包首界面中显示（0否，1 是）
    @ApiModelProperty(value = "是否在Silubium钱包首界面中显示（0否，1 是）",name = "isSLBWalletShow")
    private BooleanEnum isSLBWalletShow;

    //是否在云端钱包首界面中显示
    @ApiModelProperty(value = "是否在云端钱包首界面中显示",name = "isCloudWalletShow")
    private BooleanEnum isCloudWalletShow;

    //是否在SilkTrader钱包首界面中显示
    @ApiModelProperty(value = "是否在SilkTrader钱包首界面中显示",name = "isSTWalletShow")
    private BooleanEnum isSTWalletShow;
    //排序数字
    @ApiModelProperty(value = "排序数字",name = "rank")
    private int rank;
    //所属应用ID
    private String appId;

}
