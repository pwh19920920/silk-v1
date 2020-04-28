package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author rongyu
 * @description 公告
 * @date 2018/3/5 14:59
 */
@ApiModel
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class Announcement implements Serializable {
    private static final long serialVersionUID = 7898194272883238670L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 广告位置
     */
    @ApiModelProperty(value = "广告位置(platform)", name = "announcementLocation")
    private int announcementLocation;

    @ApiModelProperty(value = "标题", name = "title")
    @NotNull(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "内容", name = "content")
    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    @ApiModelProperty(value = "创建时间", name = "createTime")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 是否显示
     */
    @ApiModelProperty(value = "是否显示，0：否，1：是", name = "isShow")
    private Boolean isShow;

    @ApiModelProperty(value = "图片链接", name = "imgUrl")
    @Column(nullable = true)
    private String imgUrl;

    @ApiModelProperty(value = "排序字段", name = "sort")
    private int sort = 0;

    @ApiModelProperty(value = "查看次数", name = "pvCount")
    /**
     * 查看次数
     */
    private int pvCount;

    /**
     * 是否全局置顶
     */
    @ApiModelProperty(value = "是否全局置顶，0：否，1：是", name = "isGlobalSort")
    private BooleanEnum isGlobalSort = BooleanEnum.IS_FALSE;

    /**
     * 是否显示到前端
     */
    @ApiModelProperty(value = "是否显示到前端，0：否，1：是", name = "isFrontShow")
    private BooleanEnum isFrontShow = BooleanEnum.IS_FALSE;

    /**
     * 外部公告链接
     */
    private String url;

    /**
     * 语言
     */
    private String languageCode;
}
