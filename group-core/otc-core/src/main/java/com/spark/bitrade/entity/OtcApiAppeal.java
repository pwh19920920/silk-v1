package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.30 09:51  
 */
@Data
@Entity
@Table(name = "otc_api_appeal")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtcApiAppeal {

    @ApiModelProperty(value = "null")
    @Id
    @GeneratedValue
    private Long id;

    /** 申诉关联者 */
    @ApiModelProperty(value = "申诉关联者")
    private Long associateId;

    /** 创建时间 */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 处理时间 */
    @ApiModelProperty(value = "处理时间")
    private Date dealWithTime;

    /** 申诉者 */
    @ApiModelProperty(value = "申诉者")
    private Long initiatorId;

    /** 发起者是否胜诉 */
    @ApiModelProperty(value = "发起者是否胜诉")
    private Integer isSuccess;

    /** 申诉理由 */
    @ApiModelProperty(value = "申诉理由")
    private String remark;

    /** 处理状态 */
    @ApiModelProperty(value = "处理状态")
    private Integer status;

    /** 处理者 */
    @ApiModelProperty(value = "处理者")
    private Long adminId;

    /** 订单id */
    @ApiModelProperty(value = "订单id")
    private Long otcApiOrderId;

    /** 申诉类型 0请求放币，1请求取消订单，2其他 */
    @ApiModelProperty(value = "申诉类型 0请求放币，1请求取消订单，2其他")
    private Integer appealType;

    @ApiModelProperty(value = "null")
    private String successRemark;

    /** 申诉取消原因描述 */
    @ApiModelProperty(value = "申诉取消原因描述")
    private String cancelDescription;

    /** 申诉取消者id */
    @ApiModelProperty(value = "申诉取消者id")
    private Long cancelId;

    /** 申诉取消原因 0已经联系上卖家，等待卖家放币，1卖家已确认到账，等待卖家放币，2买家已付款，3其他 */
    @ApiModelProperty(value = "申诉取消原因 0已经联系上卖家，等待卖家放币，1卖家已确认到账，等待卖家放币，2买家已付款，3其他")
    private Integer cancelReason;

    @ApiModelProperty(value = "null")
    private Date cancelTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 机构id */
    @TableField(value = "organization_id")
    @ApiModelProperty(value = "机构id")
    private Long organizationId;
}
