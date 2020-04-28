package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
  * Epay信息
  * @author tansitao
  * @time 2018/8/14 11:08 
  */
@Data
@Embeddable
public class Epay implements Serializable {
    private static final long serialVersionUID = 8317734763036284945L;
    /**
     * 支付宝帐号
     */
    private String epayNo;
}
