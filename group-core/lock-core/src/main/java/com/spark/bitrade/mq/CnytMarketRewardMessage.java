package com.spark.bitrade.mq;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/***
 * CNYT市场返佣的消息实体
 *
 * @author yangch
 * @time 2018.12.03 17:46
 */
@Data
public class CnytMarketRewardMessage {
    /**
     * 消息类型：处理锁仓收益、处理直推奖，处理级差奖，处理培养奖，更新业绩，更新等级
     */
    private CnytMessageType type;


    /**
     * 邀请者 （冗余属性，refInviterMarketRewardDetailId的明细中已存在）
     */
    private Long inviterMemberId;

    /**
     * 邀请者 奖励明细记录ID
     */
    private Long refInviterMarketRewardDetailId;

    /**
     * 被邀请者（冗余属性，refInviteeMarketRewardDetailId的明细中已存在）
     */
    private Long inviteeMemberId;

    /**
     * 被邀请者 奖励明细记录ID
     */
    private Long refInviteeMarketRewardDetailId;

    /**
     * 锁仓记录ID（冗余属性）
     */
    private Long refLockDetatilId;

    /**
     * 平级或越级处理状态
     */
    private CnytMessagePeerStatus peerStatus;

    /**
     * 已存在的最大奖励率（当前已处理的推荐链中的最大奖励率）
     */
    private BigDecimal existMaxRewardRate;

    /**
     * 无环推荐链记录
     */
    private ArrayList<Long> acyclicRecommendChain = new ArrayList<>();
}
