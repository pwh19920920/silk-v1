package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  * 平台消息事件
 *  * @author zhongxj
 *  * @time 2019.09.11
 *  
 */
@AllArgsConstructor
@Getter
public enum InfoType implements BaseEnum {
    /**
     * 充值到账提醒 0
     */
    COIN_IN("充值到账提醒"),
    /**
     * 新交易提醒 1
     */
    OTC_ADD_ORDER("新交易提醒"),
    /**
     * 交易即将过期 2
     */
    OTC_EXPIRE_REMIND_ORDER("交易即将过期"),
    /**
     * 已付款提醒 3
     */
    OTC_PAY_CASH("已付款提醒"),
    /**
     * 已释放提醒 4
     */
    OTC_PAY_COIN("已释放提醒"),
    /**
     * 申诉处理结果 5
     */
    OTC_APPEAL_SUCCESS("申诉处理结果"),
    /**
     * 申诉处理结果(失败用户权限被冻结)提醒 6
     */
    OTC_APPEAL_AFTER("申诉处理结果(失败用户权限被冻结)提醒"),
    /**
     * 申诉处理结果(失败被冻结前警告)提醒 7
     */
    OTC_APPEAL_BEFORE("申诉处理结果(失败被冻结前警告)提醒"),
    /**
     * C2C消息组件 8
     */
    OTC_C2C_CHAT("C2C消息组件"),
    /**
     * 商家认证审核通过 9
     */
    MERCHANT_CERTIFICATION_PASSED("商家认证审核通过"),
    /**
     * 用户发起申诉 10
     */
    OTC_ORDER_APPEAL("用户发起申诉"),
    /**
     * 交易取消（买方手动取消） 11
     */
    OTC_ORDER_MANUAL_CANCLE("交易取消（买方手动取消）"),
    /**
     * 交易取消（超时取消） 12
     */
    OTC_ORDER_AUTO_CANCLE("交易取消（超时取消）"),
    /**
     * 手动编辑站内信 13
     */
    MANUAL_INSTATION("手动编辑站内信"),
    /**
     * 新交易提醒（广告方） 14
     */
    OTC_ADD_ORDER_TO_ADVERTISE("新交易提醒（广告方）"),
    /**
     * 申诉处理结果(胜诉方) 15
     */
    OTC_APPEAL_WIN("申诉处理结果(胜诉方)"),
    /**
     * 收到好友赠送矿石 16
     */
    NEW_YEAR_RECEIVE_STONE("收到好友赠送矿石");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }

    public static InfoType valueOfOrdinal(int ordinal) {
        InfoType[] values = InfoType.values();
        for (InfoType infoType : values) {
            int o = infoType.getOrdinal();
            if (o == ordinal) {
                return infoType;
            }
        }
        return null;
    }
}
