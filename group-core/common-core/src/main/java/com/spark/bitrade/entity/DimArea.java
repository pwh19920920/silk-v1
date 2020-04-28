package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
  * 定位信息实体类
  * @author tansitao
  * @time 2018/5/14 15:41 
  */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class DimArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    private String areaId;

    //区域名
    private String areaAbbrName;

    /**
     * 区域全称名
     */
    private String areaName;

    /**
     * 父id
     */
    private String fatherId;

    private String father_id2;

    /**
     * 等级
     */
    private int level;

}
