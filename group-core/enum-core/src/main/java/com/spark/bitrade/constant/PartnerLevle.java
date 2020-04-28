package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 合伙人等级
  * @author tansitao
  * @time 2018/5/28 13:56 
  */
@AllArgsConstructor
@Getter
public enum PartnerLevle implements BaseEnum {

    /**
     * 省级合伙人
     */
    M1("省级合伙人"),
    /**
     * 市合伙人
     */
    M2("市合伙人"),
    /**
     * 区合伙人
     */
    M3("区合伙人"),
    /**
     * 县合伙人
     */
    M4("县合伙人");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
