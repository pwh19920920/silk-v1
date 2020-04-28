package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
 * 任务类型
 * @author yangch
 * @time 2018.09.13 9:39
 */

@AllArgsConstructor
@Getter
public enum BettingStateOperateType implements BaseEnum {
    OP_REWARD(0, "分红返佣操作"),
    OP_MARK_GUESS_PRICE(1,"开奖通知"),
    OP_PRAISE(2, "中奖奖励操作"),
    OP_PRAISE_NOTIFICATION(3, "中奖通知"),
    OP_BUY_BACK(4,"回购操作"),
    OP_REDPACKET(5,"奖池红包扣除通知"),
    OP_AUTO_ABANDON_PRIZE(6,"弃奖操作通知"),
    OP_REDPACKET_END(7,"红包结束通知"),
    OP_NEXT_JACKPOT(8,"下期奖池沉淀操作"),
    OP_REDPACKET_READY(9,"红包准备操作");

    //操作类型(分红返佣,回购,奖励,红包扣除,下期奖池沉淀扣除,抢红包)

    @Setter
    private int code;

    @Setter
    private String nameCn;

    @Override
    @JsonValue
    public int getOrdinal() {
        return code;
    }

    public static BettingStateOperateType valueOfOrdinal(int ordinal){
        switch (ordinal){
            case 0: return OP_REWARD;
            case 1: return OP_MARK_GUESS_PRICE;
            case 2: return OP_PRAISE;
            case 3: return OP_PRAISE_NOTIFICATION;
            case 4: return OP_BUY_BACK;
            case 5: return OP_REDPACKET;
            case 6: return OP_AUTO_ABANDON_PRIZE;
            case 7: return OP_REDPACKET_END;
            case 8: return OP_NEXT_JACKPOT;
            case 9: return OP_REDPACKET_READY;
        }
        return null;
    }
}
