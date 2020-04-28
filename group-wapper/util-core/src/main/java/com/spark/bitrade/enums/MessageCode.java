package com.spark.bitrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 *  编码枚举
 *
 *【规则说明】
 *    4位编码规则=[2位类型]+[2位序号]
 *
 *  《类型》：
 *    权限类错误=3
 *    请求类错误=4（参数、请求类型等）
 *    用户模块=5
 *    帐户模块=6
 *    场外交易模块=7
 *    币币交易模块=8
 *    未知错误=9
 *
 *    eg：
 *    1、权限登陆错误 =3001
 *    2、请求参数错误=4001
 *
 *《注意》
 *    1、0 = 成功
 *    2、500 = 未知错误
 *    429   请求太频繁，请稍后再试
 *    999   系统升级
 *    4000  未授权的访问
 *    3000  参数绑定错误(如:必须参数没传递)
 *    6000  数据过期，请刷新重试
 *
 * @author yangch
 * @time 2019.01.23 16:22
 */
@AllArgsConstructor
@Getter
public enum MessageCode {
    SUCCESSED(0, "SUCCESSED", "SUCCESS"),
    ERROR(500, "ERROR", "未知错误"),
    UNKNOW_ERROR(500, "UNKNOW_ERROR", "未知错误"),

    //权限类错误=3
    INVALID_AUTH_TOKEN(3011, "INVALID-AUTH-TOKEN", "无效的访问令牌"),
    AUTH_TOKEN_TIMEOUT(3012, "AUTH-TOKEN-TIME-OUT", "访问令牌已过期"),


    //请求类错误=4（参数、请求类型等）
    REQUEST_BAD(4041, "REQUEST_BAD", "请求错误"),
    INVALID_PARAMETER(4444, "INVALID_PARAMETER", "参数无效"),
    INVALID_PARAMETER_SIZE(4001, "INVALID_PARAMETER_SIZE", "无效的参数数量"),
    REQUIRED_PARAMETER(4002, "REQUIRED_PARAMETER", "参数不能为空"),
    BAD_PARAMETER_TYPE(4003, "BAD_PARAMETER_TYPE", "提供的参数类型有误"),
    BAD_PARAMETER_FORMAT(4004, "BAD_PARAMETER_FORMAT", "提供的参数格式有误"),
    MISSING_SIGNATURE(4005, "MISSING-SIGNATURE", "缺少签名参数"),
    MISSING_PHONE(4009, "MISSING_PHONE", "缺少手机号码"),
    MISSING_JYPASSWORD(4010, "MISSING_JYPASSWORD", "缺少资金密码"),
    MISSING_PLATFORM_MARK(4011, "MISSING_PLATFORM_MARK", "缺少平台标识"),
    BAD_FORMAT_PHONE(4030, "BAD_FORMAT_PHONE", "错误的电话号码格式"),

    //用户模块=5
    MISSING_USER(5555, "MISSING_USER","用户不存在"),
    UNAUTHORIZED_USER(5001, "UNAUTHORIZED_USER", "未授权用户"),
    ACCOUNT_DISABLE(5002, "ACCOUNT_DISABLE", "该帐号已经被禁用"),

    MEMBER_NOT_BIND(5030, "MEMBER_NOT_BIND", "该用户未与钱包绑定"),
    NO_SET_JYPASSWORD(5031, "NO_SET_JYPASSWORD", "未设置资金密码"),
    ERROR_JYPASSWORD(5032, "ERROR_JYPASSWORD", "资金密码错误"),
    RECEIVER_NEED_MERCHANT(5050, "RECEIVER_NEED_MERCHANT", "接收方不是商家"),

    //帐户模块=6
    MISSING_ACCOUNT(6666, "MISSING_ACCOUNT", "账户不存在"),
    INVALID_ACCOUNT(6001, "INVALID_ACCOUNT", "账户无效"),
    FORBID_COIN_OUT(6002, "FORBID_COIN_OUT", "禁止提币"),
    FORBID_COIN_IN(6003, "FORBID_COIN_IN", "禁止充币"),
    BAD_ACCOUNT_SIGNATURE(6004, "BAD_ACCOUNT_SIGNATURE", "账户签名校验失败"),
    ACCOUNT_BALANCE_INSUFFICIENT(6010, "ACCOUNT_BALANCE_INSUFFICIENT", "可用余额不足"),
    ACCOUNT_FROZEN_BALANCE_INSUFFICIENT(6011,"ACCOUNT_FROZEN_BALANCE_INSUFFICIENT", "冻结余额不足"),
    ACCOUNT_LOCK_BALANCE_INSUFFICIENT(6012,"ACCOUNT_LOCK_BALANCE_INSUFFICIENT", "锁仓余额不足"),
    ACCOUNT_AMOUNT_INSUFFICIENT(6013,"ACCOUNT_AMOUNT_INSUFFICIENT","转账金额不可小于手续费金额"),
    FAILED_ADD_BALANCE(6014, "FAILED_ADD_BALANCE", "增加钱包可用余额失败"),
    FAILED_SUBTRACT_BALANCE(6015, "FAILED_SUBTRACT_BALANCE", "减少钱包可用余额失败"),
    FAILED_ADD_LOCK_BLANCE(6016,"FAILED_ADD_LOCK_BLANCE","增加钱包锁仓余额失败"),
    ACCOUNT_TIMEOUT(6020, "ACCOUNT_TIMEOUT", "账户处理超时"),
    REPEAT_ORDERS(6030, "REPEAT_ORDERS", "订单重复提交"),

    INVALID_OTC_COIN(6040,"INVALID_OTC_COIN","无效的法币币种"),

    // H5游戏充提模块
    INVALID_AMOUNT(6101, "INVALID_AMOUNT","无效的数额"),
    RECORD_NOT_EXIST(6102,"RECORD_NOT_EXIST", "记录不存在"),
    INCORRECT_STATE(6103, "INCORRECT_STATE","状态不正确"),
    H5_PLATFORM_ERR(6104, "H5_PLATFORM_ERR", "H5游戏平台错误"),


    //场外交易模块=7

    //币币交易模块=8
    NONSUPPORT_FAST_EXCHANGE_COIN(8050, "NONSUPPORT_FAST_EXCHANGE_COIN", "未支持的闪兑币种"),
    MISSING_FAST_EXCHANGE_ACCOUNT(8051, "MISSING_FAST_EXCHANGE_ACCOUNT", "缺失闪兑总账户"),

    //未知错误=9
    INVALID_FEE_CONFIG(9001, "INVALID_FEE_CONFIG", "无效的手续费配置"),
    FAILED_COIN_PRICE(9011, "FAILED_COIN_PRICE", "币种价格获取失败"),

    ADDR_HAS_EXIST(9023, "ADDR_HAS_EXIST", "钱包地址已经存在"),
    WALLET_GET_FAIL(9024, "WALLET_GET_FAIL", "钱包地址获取失败"),
    INVALID_EXCHANGE_RATE(9025, "INVALID_EXCHANGE_RATE", "无效的兑换汇率"),
    NONEXISTENT_ORDER(9026, "NONEXISTENT_ORDER", "订单不存在"),
    UNMATCHED_STATUS(9027, "UNMATCHED_STATUS", "状态不匹配"),
    ;

    /**
     * 编码
     */
    private int code;

    /**
     * 英文编码
     */
    private String enCode;

    /**
     * 编码中文描述
     */
    private String desc;


    /**
     * 转换为枚举
     * @param name 枚举名称
     * @return
     */
    public static Optional<MessageCode> convertToOptionalMessageCode(String name){
        return Arrays.stream(MessageCode.values()).filter(e -> e.name().equals(name)).findFirst();
    }

    /**
     * 转换为枚举
     * @param name 枚举名称
     * @return 转换成功则返回转换后的枚举值，转换失败则返回指定的枚举值
     */
    public static MessageCode convertToMessageCode(String name, MessageCode defaultMessageCode){
        Optional<MessageCode> messageCodeOptional = convertToOptionalMessageCode(name);
        if (messageCodeOptional.isPresent()) {
            return messageCodeOptional.get();
        }

        return defaultMessageCode;
    }

    /**
     * 转换为枚举
     * @param name 枚举名称
     * @return 转换成功则返回转换后的枚举值，转换失败则返回未知错误的枚举值
     */
    public static MessageCode convertToMessageCode(String name){
        return convertToMessageCode(name, MessageCode.ERROR);
    }
}
