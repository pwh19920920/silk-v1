package com.spark.bitrade.entity;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.annotation.Excel;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.util.DateUtil;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

/**
  * 归集日志表
  * @author tansitao
  * @time 2018/5/12 9:19 
  */
@Entity
@Data
@Table(name = "CollectionLog")
@ExcelSheet
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    private Long id;
    /**
     * 归集时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private Date createTime;

    @Transient
    @Excel(name = "归集时间")
    private String time ;
    /**
     * 发送地址
     */
    @Excel(name = "发送地址")
    private String fromAddress;
    /**
     * 接收地址
     */
    @Excel(name = "接收地址")
    private String toAddress;
    /**
     * 金额
     */
    @Excel(name = "金额")
    @Column(columnDefinition = "decimal(19,8) comment '金额'")
    private BigDecimal amount;
    /**
     * 手续费
     */
    @Excel(name = "手续费")
    @Column(columnDefinition = "decimal(19,8) comment '手续费'")
    private BigDecimal fee;
    /**
     * 币种
     */
    @Excel(name = "币种")
    private String coin;
    /**
     * 备注
     */
    @Excel(name = "备注")
    private String comment;


}
