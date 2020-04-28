package com.spark.bitrade.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 支付方式配置
 * </p>
 *
 * @author qiliao
 * @since 2020-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class PaySetting implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private Long id;

    /**
     * 支付方式名称
     */
    @ApiModelProperty(value = "支付方式名称")
    private String payName;

    /**
     * 支付方式键
     */
    @ApiModelProperty(value = "支付方式键")
    private String payKey;

    /**
     * 是否包含外部文件引用(0否1是)
     */
    @ApiModelProperty(value = "是否包含外部文件引用(0否1是)")
    private String appendFile;

    /**
     * 外部文件引用字段
     */
    @ApiModelProperty(value = "外部文件引用字段")
    private String fileJoinField;

    /**
     * 支付方式是否启用（0否1是）
     */
    @ApiModelProperty(value = " 支付方式是否启用（0否1是）")
    private String payState;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private Long createId;

    /**
     * 更新人
     */
    private Long updateId;



}
