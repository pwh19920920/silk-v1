package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 申诉类型
  * @author tansitao
  * @time 2018/9/7 10:32 
  */
@AllArgsConstructor
@Getter
public enum AppealType implements BaseEnum {

    RELEASE("请求放币"),

    CANCLE("请求取消订单"),

    OTHER("其他");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }


    public static AppealType findOfOrdinal(int o){
        AppealType[] values = AppealType.values();
        for (AppealType type:values){
            if (type.getOrdinal()==o){
                return type;
            }
        }

        return null;
    }

}
