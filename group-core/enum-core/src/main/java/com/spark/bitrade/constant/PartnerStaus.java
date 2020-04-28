package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 合伙人状态
  * @author tansitao
  * @time 2018/5/28 13:56 
  */
@AllArgsConstructor
@Getter
public enum PartnerStaus implements BaseEnum {

    /**
     * 正常
     */
    normal("正常"),
    /**
     *
     */
    disable("禁用");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
