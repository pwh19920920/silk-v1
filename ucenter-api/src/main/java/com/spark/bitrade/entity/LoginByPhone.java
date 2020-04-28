package com.spark.bitrade.entity;

import com.spark.bitrade.constant.LoginType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Zhang Jinwei
 * @date 2018年01月08日
 */
@Data
public class LoginByPhone {
    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+任意数
     * 17+任意数
     * 147
     */
    /*@Pattern(regexp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(147))\\d{8}$", message = "{LoginByPhone.phone.pattern}")*/
    @NotBlank(message = "{LoginByPhone.phone.null}")
    private String phone;

    @NotBlank(message = "{LoginByPhone.password.null}")
    //@Length(min = 6, max = 20, message = "{LoginByPhone.password.length}") //已更改前端密码明文传输为md5的密文，此处会误验证
    private String password;

    @NotBlank(message = "{LoginByPhone.username.null}")
    @Length(min = 3, max = 64, message = "{LoginByPhone.username.length}")
    private String username;

    @NotBlank(message = "{LoginByPhone.country.null}")
    private String country;


    @NotBlank(message = "{LoginByPhone.code.null}")
    private String code;

    private String promotion;

    private LoginType loginType;//add by tansitao 时间： 2018/12/26 原因：注册终端类型

    //钱包标志id
    private String walletMarkId;


    private String newRegiest;

}
