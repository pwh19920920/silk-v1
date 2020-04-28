package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 增加协议实体
 * @author tansitao
 * @time 2018/4/25 14:38 
 */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class Agreement {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull(message = "标题不能为空")
    private String title;

    @Column(columnDefinition="TEXT")
    @Basic(fetch=FetchType.LAZY)
    private String content;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //是否显示
    private Boolean isShow;

    @Column(nullable = true)
    private String imgUrl;

    private int sort = 0 ;
}
