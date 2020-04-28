package com.spark.bitrade.constant;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 转让状态
 * @author Zhang Yanjun
 * @time 2019.01.09 16:10
 */
@AllArgsConstructor
@Getter
public enum PayTransferStatus implements BaseEnum,IEnum {

    sponsor("发起"),SUCCESS("成功"),FAIL("失败");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
    @Override
    public Serializable getValue() {
        return this.ordinal();
    }
}
