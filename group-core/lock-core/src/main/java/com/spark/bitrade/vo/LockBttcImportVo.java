package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.25 13:38  
 */
@Data
public class LockBttcImportVo {

    @ApiModelProperty(value = "导入时间")
    private String importTime;
    @ApiModelProperty(value = "数量")
    private BigDecimal amount;
    @ApiModelProperty(value = "用户id")
    private Long memberId;
}
