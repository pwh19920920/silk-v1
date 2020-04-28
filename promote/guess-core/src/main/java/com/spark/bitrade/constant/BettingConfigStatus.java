package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 活动状态
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum BettingConfigStatus implements BaseEnum {
    STAGE_INVALID(0,"未生效"),
    STAGE_PREPARE(1, "未开始"),
    STAGE_VOTING(2, "投票中"),
    STAGE_WAITING(3, "待开奖"),
    STAGE_PRIZING(4, "领奖中"),
    STAGE_FINISHED(5, "已完成");
    //1、活动状态：①.未生效；②.未开始；③投票中；④、待开奖；⑤.领奖中；⑥.已完成

    @Setter
    private int code;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return code;
    }
}
