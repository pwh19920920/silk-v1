package com.spark.bitrade.mq;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/****
 * 消息类型
 * @author yangch
 * @time 2018.12.03 17:59
 */
@AllArgsConstructor
@Getter
public enum CnytMessageType implements BaseEnum {
    //消息类型：处理锁仓收益，处理直推奖，处理级差奖，处理培养奖，更新业绩，更新等级，实时返佣
    INCOME_LIQUIDATION("处理锁仓收益"),//0

    PUSH_REWARD("处理直推奖"),//1

    DIFFER_REWARD("处理级差奖"), //2

    CULTIVATE_REWARD("处理培养奖"),//3

    PERFORMANCE("更新业绩"),//4

    LEVEL("更新等级"),//5

    REALTIME_REWARD("实时返佣")
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
