package com.spark.bitrade.vo;

import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * c2c交易手续费综合查询VO
 * @author Zhang Yanjun
 * @time 2018.10.10 17:19
 */
@Data
@ExcelSheet
@ApiModel(description = "c2c交易手续费综合查询列表参数")
public class C2cFeeStatSynVO {
    /**
     * 日期（YYYY-MM-DD）
     */
    @ApiModelProperty(name = "createTime",value = "日期")
    private String createTime;
    /**
     * 交易日期
     */
    @Excel(name = "统计周期")
    @ApiModelProperty(name = "opDate",value = "统计周期")
    private String opDate;
    /**
     * 交易类型，0：买，1：卖
     */
    @Excel(name = "交易类型")
    @ApiModelProperty(value = "交易类型导出字段",name = "typeOut")
    private String typeOut;
    @ApiModelProperty(name = "type",value = "交易类型  0：买，1：卖")
    private Integer type;//交易类型

    @Excel(name = "定价币种")
    @ApiModelProperty(name = "unit",value = "定价币种")
    private String unit;

    /**
     * 总成交数量
     */
    @Excel(name = "总C2C人民币")
    @ApiModelProperty(name = "allTradeAmount",value = "总C2C人民币")
    private BigDecimal allTradeAmount;
    /**
     * 总交易额
     */
    @Excel(name = "总C2C交易币数")
    @ApiModelProperty(name = "allTradeTurnover",value = "总C2C交易币数")
    private BigDecimal allTradeTurnover;
    /**
     * 总手续费
     */
    @Excel(name = "总C2C手续费")
    @ApiModelProperty(name = "allFee",value = "总C2C手续费")
    private BigDecimal allFee;

    /**
     * 内部成交数量
     */
    @Excel(name = "内部商户C2C人民币")
    @ApiModelProperty(name = "innerTradeAmount",value = "内部商户C2C人民币")
    private BigDecimal innerTradeAmount;
    /**
     * 内部总交易额
     */
    @Excel(name = "内部商户C2C交易币数")
    @ApiModelProperty(name = "innerTradeTurnover",value = "内部商户C2C交易币数")
    private BigDecimal innerTradeTurnover;
    /**
     * 内部手续费
     */
    @Excel(name = "内部商户C2C手续费")
    @ApiModelProperty(name = "innerFee",value = "内部商户C2C手续费")
    private BigDecimal innerFee;

    /**
     * 客户商户成交数量
     */
    @Excel(name = "客户商户C2C人民币")
    @ApiModelProperty(name = "outTradeAmount",value = "外部客户C2C人民币")
    private BigDecimal outTradeAmount;
    /**
     * 客户商户交易额
     */
    @Excel(name = "客户商户C2C交易币数")
    @ApiModelProperty(name = "outTradeTurnover",value = "外部客户C2C交易币数")
    private BigDecimal outTradeTurnover;
    /**
     * 客户商户手续费
     */
    @Excel(name = "客户商户C2C手续费")
    @ApiModelProperty(name = "outFee",value = "外部客户C2C手续费")
    private BigDecimal outFee;

}
