package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

/**
 * 场外订单申诉附件表
 * @author tansitao
 * @time 2018/8/27 17:40 
 */
@ApiModel
@Entity
@Data
@Table(name = "otc_order_appeal_accessory")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderAppealAccessory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '订单申诉ID'")
    private Long appealId;

    @ApiModelProperty(value = "图片地址",name = "urlPath")
    @Column(columnDefinition = "varchar(512) comment '附件地址'")
    private String urlPath;


    @Column(columnDefinition = "bigint(20) comment 'otcApi订单申诉ID'")
    @ApiModelProperty(value = "otcApis申诉ID")
    private Long otcApiAppealId;

}
