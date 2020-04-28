package com.spark.bitrade.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum  InternationalType {

    zh_TW("zh_HK",1),
    en_US("en_US",2),
    ko_KR("ko_KR",3);

    private String name;

    private Integer value;

    public static InternationalType nameOf(String name){
        InternationalType[] values = InternationalType.values();
        for (InternationalType type:values){
            if (name.equalsIgnoreCase(type.getName())){
                return type;
            }
        }
        return null;
    }
}
