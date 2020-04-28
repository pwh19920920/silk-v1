package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.10.16 11:08
 */
@ApiModel
@Data
public class RewardActivityVo {

    @ApiModelProperty(value = "配置id",name = "id")
    private Long id;

    @ApiModelProperty(value = "活动标题",name = "title")
    private String title;

    @ApiModelProperty(value = "来源标识",name = "tName")
    private String tName;

}
