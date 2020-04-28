package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 推荐返佣状态
 */
@AllArgsConstructor
@Getter
public enum RewardRecordStatus implements BaseEnum {
    /**
     * 未发放
     */
    UNTREATED("未发放"),
    /**
     * 发放中
     */
    TREATING("发放中"),
    /**
     * 已发放
     */
    TREATED("已发放"),
    /**
     * 发放失败
     */
    FAILED("发放失败");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
