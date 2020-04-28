package com.spark.bitrade.ext;

import lombok.Builder;

import java.util.Date;

@Builder
public class MemberClaim {
    public String subject; // 用于说明该JWT发送给的用户
    /**
     * 用于说明该JWT面向的对象(标记来源)
     */
    public String audience;
    /**
     * 用户编号
     */
    public Long userId;
    /**
     * 用户名称
     */
    public String username;
    /**
     * 生效时间
     */
    public Date issuedAt;
    /**
     * 过期时间
     */
    public Date expiresAt;
}