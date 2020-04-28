package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.DataDistType;
import lombok.Data;

import java.util.Date;


/**
 * 系统配置
 * @author Zhang Yanjun
 * @time 2019.02.27 11:06
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("silk_data_dist")
public class SilkDataDist {
  @TableId()
  private String dictId;//配置编号
  @TableId()
  private String dictKey;//配置KEY
  private String dictVal;//配置VALUE
  private String dictType;//数据类型
  private DataDistType type;//数据类型
  private String remark;//描述
  private BooleanEnum status;//状态：0-失效 1-生效
  private long sort=0;//排序
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date updateTime;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date createTime;


}
