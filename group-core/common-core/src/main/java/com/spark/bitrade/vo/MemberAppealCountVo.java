package com.spark.bitrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fumy
 * @time 2018.11.02 18:49
 */
@ApiModel
@Data
public class MemberAppealCountVo {

    @ApiModelProperty(value = "id",name = "id",dataType = "Long")
    private Long initiatorId;

    @ApiModelProperty(value = "姓名",name = "realName",dataType = "String")
    private String realName;

    @ApiModelProperty(value = "电话号码",name = "mobilePhone",dataType = "String")
    private String mobilePhone;

    @ApiModelProperty(value = "邮箱",name = "email",dataType = "String")
    private String email;

    @ApiModelProperty(value = "申诉创建时间",name = "createTime",dataType = "String")
    private String createTime;

    @ApiModelProperty(value = "申诉次数",name = "initiatorTotal",dataType = "int")
    private int initiatorTotal;

    @ApiModelProperty(value = "被申诉次数",name = "associateTotal",dataType = "int")
    private int associateTotal;

    @ApiModelProperty(value = "申诉成功次数",name = "initiatorSuccess",dataType = "int")
    private int initiatorSuccess;

    @ApiModelProperty(value = "申诉失败次数",name = "initiatorFail",dataType = "int")
    private int initiatorFail;

    @ApiModelProperty(value = "被申诉不成立次数",name = "associateSuccess",dataType = "int")
    private int associateSuccess;

    @ApiModelProperty(value = "被申诉成立次数",name = "associateFail",dataType = "int")
    private int associateFail;

    @ApiModelProperty(value = "申诉失败与被申诉成立次数合计",name = "sumTotal",dataType = "int")
    private int sumTotal;

}
