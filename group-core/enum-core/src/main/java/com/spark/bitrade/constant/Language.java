package com.spark.bitrade.constant;

import lombok.val;

/**
 * @author Administrator
 * @time 2019.09.19 14:02
 */
public enum Language {

    zh_CN("zh_CN"),
    zh_TW("zh_TW"),
    en_US("en_US"),
    ko_KR("ko_KR");




    private final String value;

    private Language(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}
