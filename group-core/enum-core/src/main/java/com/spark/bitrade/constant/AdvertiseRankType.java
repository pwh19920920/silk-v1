package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 广告排序类型
  * @author tansitao
  * @time 2018/8/27 16:28 
  */
@AllArgsConstructor
@Getter
public enum AdvertiseRankType implements BaseEnum {

    PRICE("价格"),
    AMOUNT("数量"),
    TRAN_NUM("交易笔数");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }

}
