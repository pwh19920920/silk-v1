package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@AllArgsConstructor
@Getter
public enum PromotionRewardType implements BaseEnum {
    /**
     * 0=邀请新人注册
     */
    REGISTER("邀请新人注册"),
    /**
     * 1=法币推广交易
     */
    TRANSACTION("法币推广交易"),
    /**
     * 2=币币交易
     */
    EXCHANGE_TRANSACTION("币币推广交易"),
    /**
     * 3=币币交易--合伙人返佣
     */
    PARTNER("合伙人推广佣金"),
    /**
     * 4=SLB节点产品（量化基金） 推荐奖励
     */
    ACTIVE_QUANTIFY("节点共识奖励"),
    AUTHENTICATION("协助实名认证"),
    ACTIVE_STO("STO奖励")

    ;
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
