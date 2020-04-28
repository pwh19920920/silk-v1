package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;

/**
 * 交易端第三方项目方查询数据授权信息
 * @author fumy
 * @time 2018.09.19 15:41
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThridAuthInfo {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //授权账号
    @Column(columnDefinition = "bigint(20) comment '授权账号'")
    private Long memberId;

    //授权币种
    @Column(columnDefinition = "varchar(32) comment '授权币种'")
    private String symbol;

    //状态，0，启用，1：禁用
    @Column(columnDefinition = "int(4) comment '状态，0：启用，1：禁用'")
    private int status;
}
