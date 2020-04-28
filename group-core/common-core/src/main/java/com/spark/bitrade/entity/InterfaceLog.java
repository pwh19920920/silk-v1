package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.AdminModule;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
  * 接口调用日志
  * @author tansitao
  * @time 2018/5/2 16:27 
  */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfaceLog {

    //主键id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //接口地址
    private String url;

    //请求参数
    private String requestParam;

    //返回参数
    private String responseParam;

    //备注
    private String remark;
    //访问接口时间
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reqestTime;

//    @CreationTimestamp
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date endTime;

}
