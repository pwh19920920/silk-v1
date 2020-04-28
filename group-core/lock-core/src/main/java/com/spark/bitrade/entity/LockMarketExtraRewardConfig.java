package com.spark.bitrade.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import com.spark.bitrade.constant.LockMarketExtraRewardConfigStatusEnum;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * (LockMarketExtraRewardConfig)实体类
 *
 * @author makejava
 * @since 2019-03-19 15:40:21
 */
@Data
@TableName("lock_market_extra_reward_config")
public class LockMarketExtraRewardConfig implements Serializable {

    private static final long serialVersionUID = -10949150533653810L;
    /**
     *  id
     */
    private Long id;
    /**
     * 会员id
     */
    private Long memberId;
    /**
     * 奖励率(0-1)
     */
    private Double rewardRate;
    /**
     * 状态{0:失效,1:生效}
     */
    private LockMarketExtraRewardConfigStatusEnum status;
    /**
     * 币种
     */
    private String symbol;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}