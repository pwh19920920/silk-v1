package com.spark.bitrade.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author rongyu
 * @description 系统广告
 * @date 2018/1/6 15:06
 */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysAdvertise {

    /**
     * 编号
     */
    @Id
    private String serialNumber;

    /**
     * 名称
     */
    @Excel(name = "系统广告名称", orderNum = "1", width = 20)
    @NotBlank(message = "名称不得为空")
    private String name;

    /**
     * 广告位置
     */
    @Excel(name = "系统广告位置", orderNum = "1", width = 20)
    @NotNull(message = "广告位置不得为空")
    private SysAdvertiseLocation sysAdvertiseLocation;

    /**
     * 开始时间
     */
    @Excel(name = "开始时间", orderNum = "1", width = 20)
    @NotBlank(message = "开始时间不能空")
    private String startTime;

    /**
     * 结束时间
     */
    @Excel(name = "结束时间", orderNum = "1", width = 20)
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /**
     * 图片链接url
     */
    @Excel(name = "url", orderNum = "1", width = 20)
    @NotBlank(message = "url不能为空")
    private String url;
    /**
     * 跳转地址
     */
    private String linkUrl;

    /**
     * 备注
     */
    private String remark;

    @NotNull(message = "状态不得为空")
    private CommonStatus status = CommonStatus.NORMAL;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    private String author;

    private int sort = 0;

    /**
     * 0表示禁止跳转
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isJump = BooleanEnum.IS_FALSE;

    /**
     * 跳转类型，0：web跳转 1：app模块
     */
    private int sysAdvertiseType;

    /**
     * 语言
     */
    private String languageCode;
}
