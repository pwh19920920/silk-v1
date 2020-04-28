package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 运营手动编辑站内信
 *
 * @author zhongxj
 * @date 2019.08.29
 */
@Data
@TableName("plat_instation")
public class PlatInstation {
    /**
     * 标题
     */
    private String title;

    /**
     * 接收类型
     */
    private Integer type;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 内容
     */
    private String content;

    /**
     * 部分会员的id
     */
    private String memberIds;

    /**
     * 发送时间
     */
    private Date sendTime;
}
