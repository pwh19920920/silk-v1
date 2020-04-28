package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.PartnerLevle;
import com.spark.bitrade.constant.PartnerStaus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
  * 区域合伙人
  * @author tansitao
  * @time 2018/5/28 12:04 
  */
@Entity
@Data
@Table
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerArea {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 合伙人会员信息
     */
    @JoinColumn(name = "member_id",nullable = false)
    @ManyToOne
    private Member member;

    /**
     * 合伙人区域信息
     */
    @JoinColumn(name = "area_id",nullable = false)
    @ManyToOne
    private DimArea dimArea;

    /**
     * 等级
     */
    @Enumerated(EnumType.ORDINAL)
    private PartnerLevle level;

    /**
     * 状态
     */
    @Enumerated(EnumType.ORDINAL)
    private PartnerStaus partnerStaus;

    /**
     * 创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creatTime;
}
