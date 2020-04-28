package com.spark.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * app启动界面信息
 * @author Zhang Yanjun
 * @time 2018.12.12 13:48
 */
@ApiModel
@Data
public class AppStartImgVo {

    @ApiModelProperty(name = "name", value = "图片名字")
    private String name;
    @ApiModelProperty(name = "imgUrl", value = "图片地址")
    private String imgUrl;
    @ApiModelProperty(name = "url", value = "跳转链接")
    private String url;
    @ApiModelProperty(name = "url", value = "显示时长")
    private Long duration;
    @ApiModelProperty(name = "isShow", value = "是否显示  0否 1是")
    private BooleanEnum isShow;
    @ApiModelProperty(name = "isFirst", value = "优先显示 0否 1是")
    private BooleanEnum isFirst;
    @ApiModelProperty(name = "showTimeStart", value = "图片显示时间开始")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date showTimeStart;
    @ApiModelProperty(name = "showTimeEnd", value = "图片显示时间结束")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date showTimeEnd;
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
