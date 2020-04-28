package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 万份收益记录表(LockHqbThousandsIncomeRecord)实体类
 *
 * @author dengdy
 * @since 2019-04-23 15:52:37
 */
@Data
@TableName("lock_hqb_thousands_income_record")
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "万份收益记录表")
public class LockHqbThousandsIncomeRecord implements Serializable {

    private static final long serialVersionUID = -25829076295327510L;

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "id", name = "id")
    private String id;

    /**
     * 应用或渠道ID
     */
    @ApiModelProperty(value = "应用或渠道ID", name = "appId")
    private String appId;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种", name = "coinSymbol")
    private String coinSymbol;

    /**
     * 更新日期
     */
    @ApiModelProperty(value = "更新日期", name = "updateTime")
    private Long updateTime;

    /**
     * 万份收益
     */
    @ApiModelProperty(value = "万份收益", name = "tenThousandIncome")
    private BigDecimal tenThousandIncome;

    /**
     * 数据日期 格式YYYYMMDD
     */
    @ApiModelProperty(value = "数据日期 格式YYYYMMDD", name = "opTime")
    private Long opTime;

}