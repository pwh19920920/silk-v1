package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import java.util.Date;

/**
 * 钱包与平台用户绑定关系表
 * @author tansitao
 * @time 2019/1/9 11:27 
 */
@TableName("pay_wallet_member_bind")
@Data
public class PayWalletMemberBind {
    @TableId(type = IdType.AUTO)
    private Long id;
    //钱包标识ID
    private String walletMarkId;
    //用户ID
    private Long memberId;
    //绑定时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTime;
    //钱包鉴权验证token
    private String token;
    //收款角色ID，默认为1，角色1必须为默认角色
    private Long roleId = 1L;
    //token过期时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date tokenExpireTime;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //数据是否有效
    private BooleanEnum usable = BooleanEnum.IS_TRUE;
    //商家(圈)名称
    private String businessName;
    //所属应用ID
    private String appId;
}
