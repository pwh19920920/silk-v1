package com.spark.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.spark.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/***
  * 监控规则触发事件类型
 *
  * @author yangch
  * @time 2018.11.01 9:34
 * @since 1.3版本新增，仅实现 申诉失败 和 取消订单
 */
@AllArgsConstructor
@Getter
public enum MonitorTriggerEvent implements BaseEnum {
    //登陆类事件
    LOGIN("LOGIN", "登陆"),//0
    LOGIN_SUCCESSED("LOGIN", "登陆成功"),//1
    LOGIN_FAILED("LOGIN", "登陆失败"),//2

    //登出类事件
    LOGOUT("LOGOUT", "登出"),//3
    LOGOUT_SUCCESSED("LOGOUT", "登出成功"),//4
    LOGOUT_FAILED("LOGOUT", "登出失败"),//5

    //c2c触发事件：下单（下单成功，下单失败）、申诉失败（提交申诉、申诉成功）
    // 、撤销订单（成功、失败）、主动撤销订单、被动撤单订单）、放款、放行
    OTC_ADD_ORDER("OTC", "创建C2C订单"),//6
    OTC_ADD_ORDER_SUCCESSED("OTC", "C2C下单成功"),//7
    OTC_ADD_ORDER_FAILED("OTC", "C2C下单失败"),//8

    OTC_APPEAL_SUBMIT("OTC", "提交C2C订单申诉"),//9
    OTC_APPEAL_COMPLETE("OTC", "C2C订单申诉处理"),//10 //edit by tansitao 时间： 2018/12/24 原因：修改备注和文字
    OTC_APPEAL_CANCEL("OTC", "C2C订单申诉取消"),   //11//edit by tansitao 时间： 2018/12/24 原因：修改备注和文字

    OTC_CANCEL_ORDER("OTC", "撤销C2C订单"),  //12
    OTC_CANCEL_ORDER_SUCCESSED("OTC", "撤销C2C订单成功"),//13
    OTC_CANCEL_ORDER_FAILED("OTC", "撤销C2C订单失败"),//14

    OTC_AUTO_CANCEL_ORDER("OTC", "被动撤单C2C订单"),//15
    OTC_MANUAL_CANCEL_ORDER("OTC", "主动撤销C2C订单"),//16

    OTC_PAY_CASH("OTC", "C2C订单付款"),//17
    OTC_PAY_CASH_SUCCESSED("OTC", "C2C订单付款成功"),//18
    OTC_PAY_CASH_FAILED("OTC", "C2C订单付款失败"),//19

    OTC_PAY_COIN("OTC", "C2C订单放币事件"),//20
    OTC_PAY_COIN_SUCCESSED("OTC", "C2C订单放币成功"),//21
    OTC_PAY_COIN_FAILED("OTC", "C2C订单放币失败"),//22
    /**
     * 商家认证审核通过 23
     */
    BUSINESS_APPROVE("BUSINESS", "商家认证审核通过"),

    //待扩展其他类型
    EXCHANGE_CANCEL_ORDER("EXCHANGE","币币交易撤单"),//23
    ;


    @Setter
    private String cnType;  //类型
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

    public static MonitorTriggerEvent getTypeByOrdinal(int mid) {
        MonitorTriggerEvent[] values = MonitorTriggerEvent.values();
        for (MonitorTriggerEvent type : values) {
            int ordinal = type.getOrdinal();
            if (mid == ordinal) {
                return type;
            }
        }
        return null;
    }

}
