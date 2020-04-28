package com.spark.bitrade.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 财务统计所需会员账号实体类
 * @author fumy
 * @time 2018.11.08 16:54
 */
@Entity
@Data
public class FincMemberAccount {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(columnDefinition = "bigint comment '会员id'")
    private Long memberId;

    @Column(columnDefinition = "tinyint(2) comment '会员类型'")
    private int memberType;

    @Column(columnDefinition = "varchar(100) comment '会员类型描述，如：内部商户'")
    private String typeRemark;

}
