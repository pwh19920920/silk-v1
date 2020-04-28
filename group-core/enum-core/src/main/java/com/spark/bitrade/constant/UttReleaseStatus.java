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
 *  @time 2019.08.16 09:05  
 */
@AllArgsConstructor
@Getter
public enum UttReleaseStatus implements BaseEnum {
    /**
     * 释放状态 0:待释放
     */
    BE_RELEASING("待释放"),
    /**
     * 1 已释放
     */
    RELEASED("已释放"),
    /**
     * 2 已撤销
     */
    RECALLED("已撤销"),
    /**
     * 3 释放失败
     */
    RELEASE_FAILED("释放失败"),
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
