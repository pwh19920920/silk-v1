package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * <p>MemberApiSecretDTO</p>
 * @author tian.bo
 * @date 2018/12/6.
 */
@Data
public class MemberApiSecretDTO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 可用标识
     */
    private int usable;
    /**
     * 会员id
     */
    private Long memberId;
    /**
     * 访问秘钥
     */
    private String accessKey;
    /**
     * 签名秘钥
     */
    private String secretKey;
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expires;
    /**
     * 备注
     */
    private String remark;
}
