package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PromotionRewardCoin;
import com.spark.bitrade.constant.PromotionRewardCycle;
import com.spark.bitrade.constant.PromotionRewardType;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 推广奖励设置
 *
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class RewardPromotionSetting {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 如果是币币交易推广不用设置币种
     */
    @JoinColumn(name = "coin_id", nullable = true)
    @ManyToOne
    private Coin coin;
    /**
     * 启用禁用
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum status = BooleanEnum.IS_FALSE;
    @Enumerated(EnumType.ORDINAL)
    private PromotionRewardType type;
    /**
     * 推广注册：{"one": 1,"two": 0.1}
     * <p>
     * 法币暂定首次推广交易：{"one":  10,"two": 5}交易数量占比,交易数量全部换算成usdt来计算
     * <p>
     * 币币推广交易：{"one":  10,"two": 5}手续费占比
     */
    private String info;

    /**
     * 生效时间，从注册之日算起。单位天 .主要推广交易用到
     */
    private int effectiveTime = 0;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 最近更改者
     */
    @JoinColumn(name = "admin_id")
    @ManyToOne
    private Admin admin;

    //add by yangch 时间： 2018.04.26 原因：新增
    //edit by yangch 时间： 2018.04.29 原因：合并，类型变更
    @Max(value = 100)
    @Min(value = 0)
    @Transient
    private String one ;

    //add by yangch 时间： 2018.04.26 原因：新增
    //edit by yangch 时间： 2018.04.29 原因：合并，类型变更
    @Max(value = 100)
    @Min(value = 0)
    @Transient
    private String two ;

    //add by shenzucai 时间： 2018.05.25 原因：三级返佣
    @Max(value = 100)
    @Min(value = 0)
    @Transient
    private String three ;

    //add by yangch 时间： 2018.05.29 原因：返佣周期（实时、天、周、月）
    @Enumerated(EnumType.ORDINAL)
    private PromotionRewardCycle rewardCycle;

    //add by yangch 时间： 2018.05.29 原因：返佣币种（交易币、USDT、SLB）
    @Enumerated(EnumType.ORDINAL)
    private PromotionRewardCoin rewardCoin;

    private String note;//备注
    @Lob
    @Column(name = "data",columnDefinition="text")
    private String data;//活动说明

    @Column(columnDefinition="varchar(50) comment '活动标题'")
    private String title;

    @Column(columnDefinition="tinyint(2) comment '是否显示到首页'")
    private BooleanEnum isFrontShow;
}
