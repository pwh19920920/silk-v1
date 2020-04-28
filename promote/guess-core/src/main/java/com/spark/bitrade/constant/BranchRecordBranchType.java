package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum BranchRecordBranchType implements BaseEnum {
    INCOME(0, "收入"),
    DISBURSE(1, "支出");

    @Setter
    private int code;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return code;
    }
}
