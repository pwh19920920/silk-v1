package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Zhang Jinwei
 * @date 2018年03月26日
 */
@Entity
@Data
public class PreCoin{
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private String unit;

    private String remark;

    private int amount=0;

    @JsonIgnore
    @ManyToOne(targetEntity = Vote.class)
    private Vote vote;
    @JsonIgnore
    @Version
    private long version;
}
