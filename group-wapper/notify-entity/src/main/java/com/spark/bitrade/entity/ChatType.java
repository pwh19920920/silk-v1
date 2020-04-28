package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * bitrade-parent - ChatType
 *
 * @author wsy
 * @date 2019/7/3 10:03
 */
@AllArgsConstructor
@Getter
public enum ChatType implements BaseEnum {
    TEXT("文本消息"),   // 0
    IMAGE("图片消息");  // 1

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    @Override
    public String toString() {
        return String.valueOf(this.getOrdinal());
    }
}
