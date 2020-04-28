package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.Platform;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年04月24日
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppRevision {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    @Column(name = "remark",columnDefinition="text")
    private String remark;

    private String version;

    private String downloadUrl;

    @Enumerated(EnumType.ORDINAL)
    private Platform platform;

    /**
     * 最低要求版本号
     */
    private String lowestVersion;

}
