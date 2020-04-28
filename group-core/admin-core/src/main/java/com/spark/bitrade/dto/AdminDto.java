package com.spark.bitrade.dto;

import com.spark.bitrade.constant.CommonStatus;
import lombok.Data;

/**
 * @author lingxing
 * @time 2018.07.30 16:01
 */
@Data
public class AdminDto {
    //id
    private  Long id;
    //用户名
    private String username;
    //qq
    private String qq;

    private CommonStatus enable;
    //邮箱
    private String email;
    private String realName;
    //是否绑定谷歌
    private int googleState;
    //验证号码类型
    private int verificationType;
    //手机号
    private String mobilePhone;
    /**
     * 头像
     */
    private String avatar;
    //部门ID
    private String departmentId;
    //角色ID
    private  String roleId;
    //角色名
    private String role;
    //部门名
    private String departmentName;
}
