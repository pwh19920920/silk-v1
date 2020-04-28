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
 *  @time 2019.09.29 09:41  
 */
@AllArgsConstructor
@Getter
public enum  AppealSourceChannel implements BaseEnum {


    /**
     *  法币交易
     */
    OTC("法币交易"),

    /**
     *OTC-API交易
     */
    OTC_API("OTC-API交易");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }

}
