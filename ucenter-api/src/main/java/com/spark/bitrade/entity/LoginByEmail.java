package com.spark.bitrade.entity;

import com.spark.bitrade.constant.LoginType;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Zhang Jinwei
 * @date 2017年12月29日
 */
@Data
public class LoginByEmail {

    @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    private String email;

    @NotBlank(message = "{LoginByEmail.password.null}")
    // @Length(min = 6, max = 20, message = "{LoginByEmail.password.length}") //已更改前端密码明文传输为md5的密文，此处会误验证
    private String password;

    @NotBlank(message = "{LoginByEmail.username.null}")
    @Length(min = 3, max = 64, message = "{LoginByEmail.username.length}")
    private String username;

    //edit by tansitao 时间： 2018/5/12 原因：取消对国际不为空的判断
//    @NotBlank(message =  "{LoginByEmail.country.null}")
    private String country;

    // edity by yangch 2018-04-13
    private String code;

    private String promotion;

    private LoginType loginType;//add by tansitao 时间： 2018/12/26 原因：注册终端类型

    //钱包标志id
    private String walletMarkId;
}
