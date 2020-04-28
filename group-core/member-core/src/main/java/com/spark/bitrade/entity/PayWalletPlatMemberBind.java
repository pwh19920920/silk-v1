package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 钱包客户端云端账号用户管理表
 */
@TableName("pay_wallet_plat_member_bind")
@Data
public class PayWalletPlatMemberBind {
    //id
    @TableId(type = IdType.AUTO)
    private long id;
    //用户id
    private long memberId;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //收款角色ID，默认为1普通角色
    private long roleId;
    //商家名称
    private String businessName;
    //关联应用ID
    private String appId;

}
