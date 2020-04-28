package com.spark.bitrade.constant.messager;

/**
 * @author ww
 * @time 2019.09.11 17:17
 */
public enum JPushDeviceType {

    //由于 类型与 之前系统中的DeviceType 不一样 ，不知道有哪些地方有用到，所以只能重写


    ANDROID("android"),
    IOS("ios"),
    WINPHONE("winphone"),
    WEB("web"),  // ext for web
    ALL("all");

    private final String value;

    private JPushDeviceType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
