package com.spark.bitrade.vo;

import com.spark.bitrade.constant.ActivitieNumType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.entity.PayStatusInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * 会员我的资产信息
 * @author tansitao
 * @time 2018/11/21 17:16 
 */
@Data
public class MemberActivityIncomeVo {

    //add by tansitao 时间： 2018/11/20 原因：锁仓收益
    private BigDecimal lockIncome = BigDecimal.ZERO;

    //add by tansitao 时间： 2018/11/20 原因：锁仓活动数量类型
    private ActivitieNumType activitieNumType = ActivitieNumType.none;

    //add by tansitao 时间： 2018/11/20 原因：关联活动id
    private long refActivitieId;

    //会员资产
    private MemberWallet memberWallet;

    //add by tansitao 时间： 2019/1/4 原因：最小参与活动数量限制
    private BigDecimal minLimit;
}
