package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
  * 推送类型
  * @author tansitao
  * @time 2018/9/18 20:42 
  */
@AllArgsConstructor
@Getter
public enum PushType implements BaseEnum {
    JACKPOT(0, "奖池"),
    BTCPRICE(1, "比特币实时价格"),
    VOTING_SUM_INF(2, "投票区域信息"),
    REWARD(3, "领奖推送"),
    ACTIVE_STATE(4, "活动状态"),
    RED_PACKET(5, "红包中奖"),
    VOTING_INF(6, "投票成功");

    //业务类型,竞猜、抢红包

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
