package com.spark.bitrade.constant;

import lombok.Data;

/**
 * @author zhongxj
 * @time 2019.08.26
 */
public enum DeviceType {
    Android("android"),
    IOS("ios"),
    WinPhone("winphone"),
    ALL("all");

    private final String value;

    private DeviceType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
