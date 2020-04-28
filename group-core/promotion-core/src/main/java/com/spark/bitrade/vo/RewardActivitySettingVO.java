package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;

/**
 * 用户奖励
 * @author Zhang Yanjun
 * @time 2018.10.11 11:59
 */

@ApiModel(description = "用户奖励")
@Data
public class RewardActivitySettingVO {

    @ApiModelProperty(name = "id",value = "id")
    private Long id;
    /**
     * 币种
     */
    @ApiModelProperty(name = "unit",value = "币种名",required = true)
    private String unit;
    /**
     * 启用禁用
     */
    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(name = "status",value = "启用状态 0禁用，1启用")
    private BooleanEnum status = BooleanEnum.IS_FALSE;

    @ApiModelProperty(name = "type",value = "奖励类型 0注册奖励 1交易奖励 2充值奖励")
    @Enumerated(EnumType.ORDINAL)
    private ActivityRewardType type;
    /**
     * 注册奖励：{"amount": 0.5}
     */
    private String info;
    //奖励数量
    @ApiModelProperty(name = "amount",value = "奖励数量")
    private String amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(name = "updateTime",value = "更新时间")
    private String updateTime;

    @ApiModelProperty(name = "remark",value = "备注")
    private String remark;//备注

    @ApiModelProperty(name = "data",value = "活动说明")
    private String data;//活动说明

    /**
     * 最近更改者
     */
    @ApiModelProperty(name = "adminId",value = "最近更改者id")
    private long adminId;
    @ApiModelProperty(name = "adminRealName",value = "最近更改者真实姓名")
    private String adminRealName;

    //活动标题
    private String title;

    /**
     * 是否显示到首页
     */
    private BooleanEnum isFrontShow;
}
