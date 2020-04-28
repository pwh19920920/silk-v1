package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 收支角色管理表
 * （ 管理支付中的角色）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@Data
@TableName("pay_role_config")
public class PayRoleConfig {
    //角色id
    @TableId(type = IdType.AUTO)
    private Long id;
    //角色名
    private String roleName;
    //父角色id
    private Long parentId;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
