package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Desc:
 * Author: yangch
 * Version: 1.0
 * Create Date Time: 2018-05-29 22:16:00
 * Update Date Time:
 *
 * @see
 */
@Data
@Entity
@Table
public class Updating {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date starttime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endtime;

    private Integer flag;   //默认为1升级状态，非1则不是升级状态

    private String pagetemp; //升级时页面模板，h5格式

}
