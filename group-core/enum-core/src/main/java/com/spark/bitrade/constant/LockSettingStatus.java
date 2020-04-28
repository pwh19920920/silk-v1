package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 锁仓活动状态

 * @author yangch
 * @time 2018.06.12 14:51
 */
@AllArgsConstructor
@Getter
public enum LockSettingStatus implements BaseEnum {
    //0
    UNENFORCED("未生效"),
    //1
    VALID("已生效"),
    //2
    EXPIRED("已失效");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
