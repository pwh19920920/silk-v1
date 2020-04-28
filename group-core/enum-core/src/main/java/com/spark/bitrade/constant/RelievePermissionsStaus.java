package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 解冻权限任务状态
 * @author tansitao
 * @time 2018/11/27 13:48 
 */
@AllArgsConstructor
@Getter
public enum RelievePermissionsStaus implements BaseEnum {

    /**
     * 正常
     */
    unProcessed("待处理"),
    /**
     *
     */
    processed("已处理");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
