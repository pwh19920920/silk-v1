package com.spark.bitrade.dto;

import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.constant.MonitorTriggerEvent;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.11.02 14:22
 */
@ApiModel
@Data
public class MonitorRuleConfigDto {

    @ApiModelProperty(value = "配置id数组，如：1,2,4",name = "id",dataType = "String[]")
    private String id;

    @ApiModelProperty(value = "触发事件",name = "triggerEvent",dataType = "Enum")
    private MonitorTriggerEvent triggerEvent;

    //时间段（单位：分钟）
    @ApiModelProperty(value = "时间段（单位：分钟）",name = "triggerStageCycle",dataType = "int")
    private int triggerStageCycle;

    //触发次数
    @ApiModelProperty(value = "触发次数",name = "triggerTimes",dataType = "int")
    private int triggerTimes;

    //用户类别
    @ApiModelProperty(value = "用户类别",name = "triggerUserLevel",dataType = "Enum")
    private MemberLevelEnum triggerUserLevel;

    //执行的约束
    @ApiModelProperty(value = "执行的约束数组，如：1,2,4",name = "executeEvent",dataType = "String[]")
    private String executeEvent;

    //约束的有效时长（单位：分钟）
    @ApiModelProperty(value = "约束的有效时长（单位：分钟）",name = "executeDuration",dataType = "int")
    private int executeDuration;
}
