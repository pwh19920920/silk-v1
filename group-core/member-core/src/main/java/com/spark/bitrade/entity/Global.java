package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
  * 全局配置实体类
  * @author tansitao
  * @time 2018/5/11 10:27 

  */
@Data
@Embeddable
public class Global implements Serializable {
    private static final long serialVersionUID = 8317734763036284941L;

    //身份实名认证启用开关
    private int IdCardSwitch;
}
