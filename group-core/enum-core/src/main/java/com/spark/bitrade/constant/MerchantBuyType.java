package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.07.12 10:51  
 */
@Getter
@AllArgsConstructor
public enum MerchantBuyType implements BaseEnum, IEnum {


    MERCHANT_BUY("商家购币通道"), //0


    ;

    private String typeName;


    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    @Override
    public Serializable getValue() {
        return this.ordinal();
    }
}
