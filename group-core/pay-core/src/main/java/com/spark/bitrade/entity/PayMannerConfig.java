package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 支付方式配置表
 * （配置支付方式及扣款顺序）
 * @author Zhang Yanjun
 * @time 2019.01.09 15:26
 */
@TableName("pay_manner_config")
@Data
public class PayMannerConfig {
    //id
    @TableId(type = IdType.AUTO)
    private Long id;
    //序号
    private int number;
    //名称
    private String name;
    //备注
    private String comment;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
