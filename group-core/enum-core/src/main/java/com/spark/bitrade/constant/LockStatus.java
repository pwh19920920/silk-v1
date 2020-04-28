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
public enum LockStatus implements BaseEnum {

    LOCKED("已锁定"),//0

    UNLOCKED("已解锁"), //1

    CANCLE("已撤销"), //2

    UNLOCKING("解锁中") //3
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
