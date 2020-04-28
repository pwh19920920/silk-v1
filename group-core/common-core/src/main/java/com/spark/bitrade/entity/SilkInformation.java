package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 资讯模块
 *
 * @author wsy
 * @date 2019/7/1 10:33
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("silk_information")
public class SilkInformation {
    /**
     * 编号
     */
    @Id
    @TableId
    private Integer id;
    /**
     * 图片
     */
    private String thumbnail;
    /**
     * 分类
     */
    private String classify;
    /**
     * 标签
     */
    private String tags;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 状态
     */
    private BooleanEnum status;
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;
    /**
     * 发布人
     */
    private String releaseUser;
    /**
     * 阅读次数
     */
    private Integer views;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 语言代码
     */
    private String languageCode;

}
