package com.spark.bitrade.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Administrator on 2019/3/11.
 */
@AllArgsConstructor
@Getter
public enum SilkpayMessageCode {
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

            //帐户模块=6
            MISSING_ACCOUNT(6666, "MISSING_ACCOUNT", "账户不存在"),
            INVALID_ACCOUNT(6001, "INVALID_ACCOUNT", "账户无效"),
            FORBID_COIN_OUT(6002, "FORBID_COIN_OUT", "禁止提币"),
            FORBID_COIN_IN(6003, "FORBID_COIN_IN", "禁止充币"),
            BAD_ACCOUNT_SIGNATURE(6004, "BAD_ACCOUNT_SIGNATURE", "账户签名校验失败"),
            ACCOUNT_BALANCE_INSUFFICIENT(6010, "ACCOUNT_BALANCE_INSUFFICIENT", "可用余额不足"),
            ACCOUNT_FROZEN_BALANCE_INSUFFICIENT(6011,"ACCOUNT_FROZEN_BALANCE_INSUFFICIENT", "冻结余额不足"),
            ACCOUNT_LOCK_BALANCE_INSUFFICIENT(6012,"ACCOUNT_LOCK_BALANCE_INSUFFICIENT", "锁仓余额不足"),

            ACCOUNT_TIMEOUT(6020, "ACCOUNT_TIMEOUT", "账户处理超时"),
            REPEAT_ORDERS(6030, "REPEAT_ORDERS", "订单重复提交"),


            //场外交易模块=7

            //币币交易模块=8

            //未知错误=9
            INVALID_FEE_CONFIG(9001, "INVALID_FEE_CONFIG", "无效的手续费配置"),
            FAILED_COIN_PRICE(9011, "FAILED_COIN_PRICE", "币种价格获取失败"),
            FAILED_ADD_BALANCE(9021, "FAILED_ADD_BALANCE", "增加钱包可用余额失败"),
            FAILED_SUBTRACT_BALANCE(9022, "FAILED_SUBTRACT_BALANCE", "减少钱包可用余额失败"),
            ADDR_HAS_EXIST(9023, "ADDR_HAS_EXIST", "钱包地址已经存在"),
            WALLET_GET_FAIL(9024, "WALLET_GET_FAIL", "钱包地址获取失败"),

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
    public static Optional<SilkpayMessageCode> convertToOptionalMessageCode(String name){
        return Arrays.stream(SilkpayMessageCode.values()).filter(e -> e.name().equals(name)).findFirst();
    }

    /**
     * 转换为枚举
     * @param name 枚举名称
     * @return 转换成功则返回转换后的枚举值，转换失败则返回指定的枚举值
     */
    public static SilkpayMessageCode convertToMessageCode(String name, SilkpayMessageCode defaultMessageCode){
        Optional<SilkpayMessageCode> messageCodeOptional = convertToOptionalMessageCode(name);
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
    public static SilkpayMessageCode convertToMessageCode(String name){
        return convertToMessageCode(name, SilkpayMessageCode.ERROR);
    }
}
