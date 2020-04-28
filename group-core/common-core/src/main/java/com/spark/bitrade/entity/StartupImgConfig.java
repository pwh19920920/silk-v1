package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * APP启动图片配置
 * @author Zhang Yanjun
 * @time 2018.12.12 11:09
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartupImgConfig {
    /**
     * 编号
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "varchar(20) comment '图片名称'")
    private  String imageName;

    @Column(columnDefinition = "bigint(20) comment '显示时长'")
    private  Long duration;

    @Column(columnDefinition = "varchar(255) comment '点击跳转链接'")
    private  String toUrl;

    @Column(columnDefinition = "int comment '是否显示 0否，1是'")
    private BooleanEnum isShow;

    @Column(columnDefinition = "int comment '是否优先 0否，1是'")
    private BooleanEnum isFirst;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '图片显示时间范围开始'")
    private Date imageShowRangeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '图片显示时间范围结束'")
    private Date imageShowRangeEnd;

    @Column(columnDefinition = "varchar(500) comment '安卓图片地址{\"100To200\":\"http://xxxx.xx.com\",\"200To300\":\"http://xxxx.xx.com\",\"300To400\":\"http://xxxx.xx.com\"}'")
    private  String androidImageUrl;

    @Column(columnDefinition = "varchar(500) comment 'iPhone图片地址{\"100To200\":\"http://xxxx.xx.com\",\"200To300\":\"http://xxxx.xx.com\",\"300To400\":\"http://xxxx.xx.com\"}'")
    private  String iphoneImageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '添加时间'")
    @CreationTimestamp
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(columnDefinition = "datetime comment '更新时间'")
    @UpdateTimestamp
    private Date updateTime;


}
