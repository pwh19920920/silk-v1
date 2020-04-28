package com.spark.bitrade.mq;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/****
 * 级差关系
 * @author yangch
 * @time 2018.12.03 17:59
 */
@AllArgsConstructor
@Getter
public enum CnytMessagePeerStatus implements BaseEnum {
    //消息类型：级差关系/平级或越级/已处理平级或越级
    SLAVE("级差关系"),//0

    HINT_OVER_PEER("平级或越级"),//1

    HINT_CULTIVATE_REWARDED("已处理平级或越级") //2
    ;

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
