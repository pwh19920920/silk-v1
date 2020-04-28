package com.spark.bitrade.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.entity.LockCoinDetailBuilder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author fumy
 * @time 2018.12.04 09:22
 */
@ApiModel
@Data
public class StoLockDetailVo{

    LockCoinDetailBuilder lockCoinDetail;

    @ApiModelProperty(name = "incomePeroid",value = "收益分期数",dataType = "int")
    private int incomePeroid;

    @ApiModelProperty(name = "daysOfPeroid",value = "每期天数",dataType = "int")
    private int daysOfPeroid;

    @ApiModelProperty(name = "nextRewardTime",value = "下期收益到账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextRewardTime;

    @ApiModelProperty(name = "nextRewardTurnover",value = "下期收益数量",dataType = "bigint")
    private BigDecimal nextRewardTurnover;

}
