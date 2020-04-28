package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.10.16 15:31
 */
@ApiModel
@Data
public class RewardActivityResp {

    @ApiModelProperty(value = "活动标题",name = "title")
    private String title;

    @ApiModelProperty(value = "富文本内容",name = "content")
    private String content;
}
