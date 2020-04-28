package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.PromotionLevel;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberPromotion {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //邀请者Id
    private Long inviterId;
    //受邀者Id
    private Long inviteesId;

    @Enumerated(EnumType.ORDINAL)
    private PromotionLevel level;
    /**
     * 创建时间
     */
    @CreationTimestamp
    private Date createTime;
    /**
     * 最近一次修改时间
     */
    @CreationTimestamp
    private Date updateTime;
}
