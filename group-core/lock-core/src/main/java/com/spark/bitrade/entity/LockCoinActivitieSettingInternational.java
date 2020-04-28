package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author zhouhaifeng
 * @time 2019.11.04 10:10
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class LockCoinActivitieSettingInternational {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /** 活动方案名称 */
    @Column(columnDefinition = "varchar(255) comment '活动方案名称'")
    private String name;

    /** 活动备注*/
    @Column(columnDefinition = "text comment '活动内容（富文本）'")
    private String note;

    //国际化类型(1：繁体中文，2：英文，3：韩文)
    @Column(columnDefinition = "int(11) comment '国际化类型(1：繁体中文，2：英文，3：韩文)'")
    private Integer internationalType;

    //与lock_coin_activitie_setting表的id关联
    @Column(columnDefinition = "bigint(20) comment '与lock_coin_activitie_setting表的id关联'")
    private Long settingId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    //是否删除（0：否，1：是）
    @Column(columnDefinition = "int(11) comment '是否删除（0：否，1：是）'")
    private Integer del;
}
