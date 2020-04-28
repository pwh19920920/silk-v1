package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 国家
 *
 * @author Zhang Jinwei
 * @date 2018年02月10日
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Country implements Serializable {
    /**
     * 中文名称
     */
    @Id
    private String zhName;

    /**
     * 英文名称
     */
    private String enName;

    /**
     * 区号
     */
    private String areaCode;
    /**
     * 语言
     */
    private String language;

    /**
     * 当地货币缩写
     */
    private String localCurrency;

    private int sort;

    private  Integer enabled;
}
