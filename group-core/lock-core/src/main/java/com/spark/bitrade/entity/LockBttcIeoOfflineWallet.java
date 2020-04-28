package com.spark.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * (LockBttcIeoOfflineWallet)实体类
 *
 * @author huyu
 * @since 2019-06-24 09:24:51
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "IEO活动金钥匙账户类")
public class LockBttcIeoOfflineWallet {
    //id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(name = "id",value = "id")
    private Long id;
    /**
     *  币种地址
     */
    @ApiModelProperty(name = "address",value = "币种地址")
    private String address;
    /**
     * 余额
     */
    @ApiModelProperty(name = "balance",value = "余额")
    private BigDecimal balance;
    /**
     * 会员id
     */
    @ApiModelProperty(name = "memberId",value = "会员id")
    private Long memberId;
    /**
     * 币种id
     */
    @ApiModelProperty(name = "coinId",value = "币种id")
    private String coinId;
    /**
     * 上次金钥匙释放数量，用于保证事务的一致性
     */
    @ApiModelProperty(name = "lastReleaseAmount",value = "上次金钥匙释放数量")
    private BigDecimal lastReleaseAmount;
    /**
     * 已解数量
     */
    @ApiModelProperty(name = "unlockedAmount",value = "已解数量")
    private BigDecimal unlockedAmount;
    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime",value = "创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty(name = "updateTime",value = "更新时间")
    private Date updateTime;

}