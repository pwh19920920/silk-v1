package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年02月26日
 */
@AllArgsConstructor
@Getter
public enum BusinessApplyStatus implements BaseEnum {

    APPLYING("申请中"),

    PASS("通过"), //1

    NOPASS("未通过")//2
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
