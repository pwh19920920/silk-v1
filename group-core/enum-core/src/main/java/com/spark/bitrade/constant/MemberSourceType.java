package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 会员来源类型
  * @author tansitao
  * @time 2018/12/26 10:33 
  */
@AllArgsConstructor
@Getter
public enum MemberSourceType implements BaseEnum {

    SILK_EXCHANGE("星客交易所会员"),//0
    SHENGTENG("升腾会员"),//1
    LIANREN("链人会员"),//2
    SILK_FINANCE("星客CNYT理财会员"),//3
    SILK_WALLET("星客钱包会员");//4

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
