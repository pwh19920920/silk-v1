package com.spark.bitrade.model;

import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

/**
 * @author zhangyanjun
 * @date 2018年10月31日
 */
@Data
@ApiModel
public class AppealDealWithScreen {
    @NotNull(message = "申诉id不能为空")
    @ApiModelProperty(value = "申诉id",name = "appealId")
    private Long appealId;

    @ApiModelProperty(value = "订单编号",name = "orderSn")
    private String orderSn;

    @ApiModelProperty(value = "图片地址",name = "urlPath")
    private String urlPath;

    //胜诉方id
    @ApiModelProperty(value = "胜诉方id",name = "successId")
    private Long successId;
    //胜诉原因
    @ApiModelProperty(value = "胜诉原因",name = "successRemark")
    private String successRemark;

//    败方id
    @ApiModelProperty(value = "败方id",name = "failId")
    private Long failId;

    //是否冻结败方id 0否 1是
    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(value = "是否冻结败方id 0否 1是",name = "isFrozen")
    private BooleanEnum isFrozen=BooleanEnum.IS_FALSE;
}
