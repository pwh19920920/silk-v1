package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.08.16 09:03  
 */
@AllArgsConstructor
@Getter
public enum UttMemberStatus implements BaseEnum {
    /**
     * 0 待处理
     */
    PENDING("待处理"),
    /**
     * 1 已处理
     */
    PROCESSED("已处理"),
    /**
     * 2 处理失败
     */
    PROCESSED_FAILED("处理失败"),

    ;

    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
