package com.spark.bitrade.constant;

/**
 * 系统常量
 *
 * @author Zhang Jinwei
 * @date 2017年12月18日
 */
public class SysConstant {
    /**
     * session常量
     */
    public static final String SESSION_ADMIN = "ADMIN_MEMBER";
    public static final String SESSION_MEMBER = "API_MEMBER";
    /**
     * 验证码
     */
    public static final String PHONE_REG_CODE_PREFIX = "PHONE_REG_CODE_";
    public static final String PHONE_RESET_TRANS_CODE_PREFIX = "PHONE_RESET_TRANS_CODE_";
    public static final String PHONE_LOGIN_CODE = "PHONE_LOGIN_CODE_";
    public static final String PHONE_BIND_CODE_PREFIX = "PHONE_BIND_CODE_";
    public static final String PHONE_UPDATE_PASSWORD_PREFIX = "PHONE_UPDATE_PASSWORD_";
    public static final String EMAIL_COMMON_CODE_PREFIX = "EMAIL_COMMON_CODE_";
    public static final String PHONE_ADD_ADDRESS_PREFIX = "PHONE_ADD_ADDRESS_";
    public static final String EMAIL_BIND_CODE_PREFIX = "EMAIL_BIND_CODE_";
    public static final String PHOME_ORIGINAL_CODE_PERFIX = "PHOME_ORIGINAL_CODE_";
    /**
     * 注册验证码
     */
    public static final String EMAIL_REG_CODE_PREFIX = "EMAIL_REG_CODE_";
    public static final String ADD_ADDRESS_CODE_PREFIX = "ADD_ADDRESS_CODE_";
    public static final String RESET_PASSWORD_CODE_PREFIX = "RESET_PASSWORD_CODE_";
    public static final String PHONE_CHANGE_CODE_PREFIX = "PHONE_CHANGE_CODE_";
    public static final String ADMIN_LOGIN_PHONE_PREFIX = "ADMIN_LOGIN_PHONE_PREFIX_";
    public static final String ADMIN_COIN_REVISE_PHONE_PREFIX = "ADMIN_COIN_REVISE_PHONE_PREFIX_";
    public static final String ADMIN_COIN_TRANSFER_COLD_PREFIX = "ADMIN_COIN_TRANSFER_COLD_PREFIX_";
    public static final String ADMIN_EXCHANGE_COIN_SET_PREFIX = "ADMIN_EXCHANGE_COIN_SET_PREFIX_";
    /**
     * edit by yangch 时间： 2018.04.24 原因：合并时发现已删除，暂时保留
     */
    public static final String ADMIN_ID_PREFIX = "ADMIN_ID_";
    /**
     * add by tansitao 时间： 2018/6/1 原因：新增区域前缀
     */
    public static final String AREA_PREFIX = "AREA_PREFIX_";
    /**
     * 防攻击验证
     */
    public static final String ANTI_ATTACK_ = "ANTI_ATTACK_";
    /**
     * 用户登录信息,0,表示已登录，1表示已经退出
     */
    public static final String MEMBER_LOGOUT = "spring:session:logout:";
    /**
     * 最新交易缓存key
     */
    public static final String TRADE_PLATE_MAP = "entity:trade:plateMap:";
    /**
     * 第三方平台token
     */
    public static final String THIRD_TOKEN = "entity:third:token:";
    /**
     * C2C防止重复提交订单标志
     */
    public static final String C2C_DEALING_ORDER = "entity:otcOrder:";
    /**
     * C2C中的订单监控key
     */
    public static final String C2C_MONITOR_ORDER = "busi:monitor:otcOrder:";
    /**
     * C2C中的订单数
     */
    public static final String C2C_ONLINE_NUM = "onlineNum";
    /**
     * C2C未处理事件消息列表前缀
     */
    public static final String NOTICE_OTC_EVENT_PREFIX = "notice:otc:envent:";
    /**
     * C2C即将过期列表前缀
     */
    public static final String OTC_EXPIRE_REMIND = "job:otc:expire:remind:";
    /**
     * C2C未读聊天消息列表前缀
     */
    public static final String NOTICE_OTC_CHAT_PREFIX = "notice:otc:chat:";
    /**
     * 系统通知列表前缀
     */
    public static final String NOTICE_SYS_PREFIX = "notice:sys:";
    /**
     * 简体中文
     */
    public static final String ZH_LANGUAGE = "zh_CN";
    /**
     * 繁体中文
     */
    public static final String ZH_HK_LANGUAGE = "zh_HK";
    /**
     * 英文语言
     */
    public static final String EN_LANGUAGE = "en_US";
    /**
     * 不区分，全部（语言）
     */
    public static final String ALL = "ALL";
    /**
     * 中国
     */
    public static final String CHINA = "中国";
    /**
     * 买方
     */
    public static final Integer BUY = 1;
    /**
     * 卖方
     */
    public static final Integer SELL = 2;
    /**
     * 不限
     */
    public static final Integer NO_LIMITATION = 0;
}
