package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.*;

/**
 * 场外订单申诉处理附件表
 * @author zhangyanjun
 * @time 2018/10/31 9:33 
 */
@ApiModel
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminOrderAppealSuccessAccessory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ApiModelProperty(value = "订单申诉ID",name = "appealId")
    @Column(columnDefinition = "bigint(20) comment '订单申诉ID'")
    private Long appealId;

    @ApiModelProperty(value = "图片地址",name = "urlPath")
    @Column(columnDefinition = "varchar(512) comment '图片附件地址'")
    private String urlPath;

}
