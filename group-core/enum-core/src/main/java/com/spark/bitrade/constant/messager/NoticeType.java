package com.spark.bitrade.constant.messager;


/**
 * @author Administrator
 * @time 2019.09.05 17:51
 */


public enum NoticeType {

    NOTICE(0, NoticeTag.NOTICE, "通知"), //

    SYS_NOTICE(1, NoticeTag.SYS_NOTICE, "系统通知"), // APP H5,
    SYS_NOTICE_FORWARD(2, NoticeTag.SYS_NOTICE_FORWARD, "带URL的系统通知"),
    SYS_NOTICE_BASE(3, NoticeTag.SYS_NOTICE_BASE, "系统通知基本类型"),
    SYS_NOTICE_UNREAD_COUNT(4, NoticeTag.SYS_NOTICE_UNREAD_COUNT, "未读数量通知类型"),
    SYS_NOTICE_COIN_IN(5, NoticeTag.SYS_NOTICE_COIN_IN, "充值订单通知"),
    SYS_NOTICE_BUSINESS_VERFIY(6, NoticeTag.SYS_NOTICE_BUSINESS_VERFIY, "商家认证通知"),
    SYS_NOTICE_OTC_ORDER(7, NoticeTag.SYS_NOTICE_OTC_ORDER, "OTC订单通知"),
    SYS_NOTICE_MAIL(8, NoticeTag.SYS_NOTICE_MAIL, " 富文本站内信"),

    NOTICE_LOGIN_RETURN(9,NoticeTag.NOTICE_LOGIN_RETURN,"通知接口登录返回"),


    //以下未使用


    ORDER_NOTICE(8, NoticeTag.OTC_ORDER_NOTICE, "Otc交易通知"),
    MARKET_NOTICE(9, NoticeTag.MARKET_ORDER_NOTICE, "交易市场通知"),
    EMAIL_NOTICE(10, NoticeTag.EMAIL_NOTICE, "邮件通知"),
    CHAT_NOTICE(11, NoticeTag.CHAT_NOTICE, "聊天通知"),
    MAIL_NOTICE(12, NoticeTag.MAIL_NOTICE, "站内信通知"),
    SMS_NOTICE(13, NoticeTag.SMS_NOTICE, "短信通知"),
    JPUSH_NOTICE(14,NoticeTag.JPUSH_NOTICE,"JPUSH 极光通知"),
    BASE_NOTICE(15,NoticeTag.BASE_NOTICE ,"基础通知")
    ;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    int type = 0;
    String lable = "";
    String name = "";


    NoticeType(int type, String lable, String name) {
        this.lable = lable;
        this.type = type;
        this.name = name;
    }

}
