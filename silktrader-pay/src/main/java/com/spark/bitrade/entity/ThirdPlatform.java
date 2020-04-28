package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 三方平台实体
 * @author fumy
 * @time 2018.08.03 10:51
 */
@Entity
@Data
public class ThirdPlatform {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 平台申请秘钥
     */
    @Column(columnDefinition = "varchar(255) comment '平台申请秘钥（申请接口-加密）'")
    private String platformKey;

    /**
     * 平台名称
     */
    @Column(columnDefinition = "varchar(255) comment '平台名称'")
    private String platformName;

    /**
     * 启用状态
     */
    @Column(columnDefinition = "varchar(255) comment '启用状态(0 禁止，1启用)'")
    private Integer status;

    /**
     * 是否允许签约币种和入账币种一致，不一致则支付时需要进行币种价值转换
     */
    @Column(columnDefinition = "varchar(255) comment '是否允许签约币种和入账币种一致,0:是，1否'")
    private Integer coinCheck;

    /**
     * 折扣率
     */
    @Column(columnDefinition = "decimal(8,4) comment '折扣率'")
    private BigDecimal discount;
}
