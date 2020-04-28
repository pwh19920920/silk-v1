package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 活期宝活动配置实体类
 *
 * @author shushiping
 * @since 2019-12-09 15:34:52
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "活期宝活动配置")
public class LockHqbCoinSettgingVo extends LockHqbCoinSettging {


    /**
     * 活动名称
     */
    @ApiModelProperty(hidden = true)
    private String activityNameCn;

    /**
     * 活动名称
     */
    @ApiModelProperty(hidden = true)
    private String activityNameZhTw;

    /**
     * 活动名称
     */
    @ApiModelProperty(hidden = true)
    private String activityNameEn;

    /**
     * 活动名称
     */
    @ApiModelProperty(hidden = true)
    private String activityNameKo;



}