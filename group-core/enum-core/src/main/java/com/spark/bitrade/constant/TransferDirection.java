package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TransferDirection
 *
 * @author Archx[archx@foxmail.com]
 * @since 2019/11/23 13:48
 */
@Getter
@AllArgsConstructor
public enum TransferDirection implements BaseEnum {

    NONE("未知"), IN("转入"), OUT("转出");

    private final String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    /**
     * 支持的交易类型
     * @param type type
     * @return bool
     */
    public boolean isAvailable(TransactionType type) {
        if (this == NONE || type == null) {
            return false;
        }
        boolean ret = false;
        switch (type) {
            case FUND_TRANSFER:
            case EXCHANGE_TRANSFER:
            case OTC_TRANSFER:
            case HQB_TRANSFER:
                ret = true;
                break;
            default: // false
        }

        return ret;
    }

    public static TransferDirection of(int ordinal) {
        for (TransferDirection value : values()) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }
        return NONE;
    }
}
