package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spark.bitrade.constant.ActivityRewardType;
import com.spark.bitrade.constant.BooleanEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户奖励
 * @author Zhang Jinwei
 * @date 2018年03月08日
 */
@ApiModel
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class RewardActivitySetting {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id",value = "id")
    private Long id;

    @JoinColumn(name = "coin_id", nullable = false)
    @ManyToOne
    @ApiModelProperty(name = "coin",value = "币种")
    private Coin coin;
    /**
     * 启用禁用
     */
    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(name = "status",value = "启用状态 0禁用，1启用")
    private BooleanEnum status = BooleanEnum.IS_FALSE;

    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(name = "type",value = "奖励类型 0注册奖励 1交易奖励 2充值奖励")
    private ActivityRewardType type;
    /**
     * 注册奖励：{"amount": 0.5}
     * <p>
     *
     */
    @ApiModelProperty(name = "info",value = "奖励信息",hidden = true)
    private String info;
    @Transient
    @ApiModelProperty(name = "amount",value = "奖励数量")
    private BigDecimal amount ;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(name = "updateTime",value = "更新时间")
    private Date updateTime;

    /**
     * 最近更改者
     */
    @JoinColumn(name = "admin_id")
    @ManyToOne
    @ApiModelProperty(name = "admin",value = "更新人")
    private Admin admin;

    @Column(columnDefinition="varchar(255) comment '备注'")
    private String remark;//edit by zyj:备注

    @Lob
    @Column(name = "data",columnDefinition="text")
    private String data;//edit by zyj:活动说明

    @Column(columnDefinition="varchar(50) comment '活动标题'")
    private String title;

    @Column(columnDefinition="tinyint(2) comment '是否显示到首页'")
    private BooleanEnum isFrontShow;
}
