package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年02月25日
 */
@AllArgsConstructor
@Getter
public enum RealNameStatus implements BaseEnum {
    /**
     * 未实名认证
     */
    NOT_CERTIFIED("未实名认证"),
    /**
     * 待审核
     */
    AUDITING("待审核"),
    /**
     * 审核通过
     */
    VERIFIED("审核通过"),
    /**
     * 审核未通过
     */
    AUDIT_DEFEATED("审核未通过");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
