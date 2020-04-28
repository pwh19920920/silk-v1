package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 部门锁仓汇总
 * @author Zhang Yanjun
 * @time 2018.12.04 11:02
 */
@ApiModel
@Data
public class StoLockDepVo {
    @ApiModelProperty(name = "memberId",value = "会员id",dataType = "Long")
    private Long memberId;

    @ApiModelProperty(name = "id",value = "直接部门id",dataType = "Long")
    private Long id;

    @ApiModelProperty(name = "position",value = "职务",dataType = "String")
    private String position;

    @ApiModelProperty(name = "phone",value = "部门手机号",dataType = "String")
    private String phone;

    @ApiModelProperty(name = "email",value = "部门邮箱",dataType = "String")
    private String email;

    @ApiModelProperty(name = "amount",value = "锁仓数量")
    private BigDecimal amount;

    @ApiModelProperty(name = "teamAmount",value = "团队锁仓数量")
    private BigDecimal teamAmount;

    @ApiModelProperty(name = "turnover",value = "锁仓业绩数量")
    private BigDecimal turnover;

    @ApiModelProperty(name = "teamTurnover",value = "团队锁仓业绩数量")
    private BigDecimal teamTurnover;
}