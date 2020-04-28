package com.spark.bitrade.entity.transform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.LoginType;
import com.spark.bitrade.constant.MemberLevelEnum;
import com.spark.bitrade.entity.Location;
import com.spark.bitrade.entity.Member;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2018年01月11日
 */
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "用户认证信息")
public class AuthMember implements Serializable {
    private static final long serialVersionUID = -4199550203850153635L;
    @ApiModelProperty(value = "id",name = "id")
    private long id;
    @ApiModelProperty(value = "用户名",name = "name")
    private String name;
    @ApiModelProperty(value = "真名",name = "realName")
    private String realName;
    @ApiModelProperty(value = "位置信息",name = "location")
    private Location location;
    @ApiModelProperty(value = "手机号",name = "mobilePhone")
    private String mobilePhone;
    @ApiModelProperty(value = "邮箱",name = "email")
    private String email;
    @ApiModelProperty(value = "会员等级",name = "memberLevel")
    private MemberLevelEnum memberLevel;
    @ApiModelProperty(value = "状态",name = "status")
    private CommonStatus status;
    @ApiModelProperty(value = "登录类型",name = "loginType")
    private LoginType loginType;
    @ApiModelProperty(value = "loginVerifyMap",name = "loginVerifyMap")
    private Map<String,Boolean> loginVerifyMap;
    //add by yangch 时间： 2019.02.25 原因：添加渠道来源。授权：需要传入渠道来源，附加到授权的token值上；鉴权：无session，从token附件的部分获取渠道来源
    @ApiModelProperty(value = "渠道来源",name = "platform")
    private String platform;

    /**
     * 如需添加信息在{@link #toAuthMember(Member)}方法中添加
     *
     * @param member
     * @return
     */
    public static AuthMember toAuthMember(Member member) {
        return AuthMember.builder()
                .id(member.getId())
                .name(member.getUsername())
                .realName(member.getRealName())
                .location(member.getLocation())
                .mobilePhone(member.getMobilePhone())
                .email(member.getEmail())
                .memberLevel(member.getMemberLevel())
                .status(member.getStatus())
                .build();
    }

    /***
     * 添加登录验证集合
     * @author yangch
     * @time 2018.07.07 10:19 
       * @param key
     * @param flag
     */
    public void addLoginVerifyRequire(String key,Boolean flag){
        if(loginVerifyMap==null){
            loginVerifyMap = new HashMap<>();
        }
        loginVerifyMap.put(key, flag);
    }

    public boolean verifyLoginRequire(){
        if(loginVerifyMap==null){
            return true;
        }
//        return loginVerifyMap.entrySet().stream().filter(e-> e.getValue()==false).count()>0 ?false :true;
        //谷歌手机验证二选一
        return loginVerifyMap.entrySet().stream().filter(e-> e.getValue()==true).count()>0 ?true :false;
    }

}
