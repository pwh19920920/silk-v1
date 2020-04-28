package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 活动类型
  * @author tansitao
  * @time 2018/6/14 10:18 
  */
@AllArgsConstructor
@Getter
public enum ActivitieType implements BaseEnum {
    /**
     * 锁币返币活动
     */
    LOCK("锁仓活动"),

    FINANCIAL("理财锁仓"),
    /**
     * 整存整取活动
     */
    OTHER("其它"),
    /**
     * 量化投资产品
     */
    QUANTIFY("SLB节点产品"),
    /**
     * STO锁仓
     */
    STO("STO锁仓"),
    /**
     * STO增值计划
     */
    STO_CNYT("STO增值计划"),
    /**
     * IEO锁仓活动
     */
    IEO("IEO活动"),
    /**
     * 金钥匙活动 7
     */
    GOLD_KEY("金钥匙活动"),

    /**
     * 赋能计划 8
     */
    ENERGIZE("赋能计划")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
