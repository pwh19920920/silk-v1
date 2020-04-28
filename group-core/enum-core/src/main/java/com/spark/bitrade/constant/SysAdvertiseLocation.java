package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rongyu
 * @description 系统广告位置
 * @date 2018/1/6 15:59
 */
@AllArgsConstructor
@Getter
public enum SysAdvertiseLocation implements BaseEnum {
    //app 首页轮播 0
    APP_SHUFFLING("app首页轮播"),
    //pc 首页轮播 1
    PC_SHUFFLING("pc首页轮播"),
    //pc 分类广告 2
    PC_CLASSIFICATION("pc分类广告"),
    //app首页活动 3
    APP_ACTIVITY("app首页活动"),
    //add by zyj 2019.01.07 : 理财APP相关位置
    //4
    MANAGEMENT_APP_SHUFFLING("理财APP首页轮播"),
    //5
    MANAGEMENT_APP_ACTIVITY("理财APP首页活动"),
    //add by  shenzucai 时间： 2019.03.24  原因：添加bt广告类型
    //6
    BTBANK_APP_SHUFFLING("BTBANKAPP首页轮播"),
    //7
    BTBANK_APP_ACTIVITY("BTBANKAPP首页活动"),
    //8
    SILKPAYS_SHUFFLING("silkpay首页轮播");
    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
