package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 活动支付记录的类型
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum BranchRecordBusinessType implements BaseEnum {
    BET(0, "投注"),
    REWARD(1, "返佣"),
    BUY_BACK(2,"回购"),
    GUESS_AWARD(3,"开奖奖励"),
    REDPACKET_AWARD(4,"红包奖励"),
    REDPACKET_BET(5,"红包投注"),
    SMS(6,"开奖短信订阅"),
    RED_PACKET_TOTAL(7,"红包总金额"),
    REWARD_BALANCE(8, "返佣余额");

    //业务类型,投注，返佣、回购、开奖奖励、红包奖励、红包投注、开奖短信订阅

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
