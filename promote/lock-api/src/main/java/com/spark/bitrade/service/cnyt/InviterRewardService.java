package com.spark.bitrade.service.cnyt;

import com.spark.bitrade.constant.LockRewardType;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.LockMarketLevel;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.mq.CnytMarketRewardMessage;
import com.spark.bitrade.mq.CnytMessagePeerStatus;
import com.spark.bitrade.mq.CnytMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/***
  * 
  * @author yangch
  * @time 2018.12.04 16:06
  */
@Slf4j
@Service
public class InviterRewardService {

    /**
     * 预处理推荐者的市场奖励明细记录
     *
     * @param inviteeRewardDetail 被推荐人的市场奖励明细记录
     * @param inviteeLevel        被推荐者等级
     * @param inviterMember       邀请者信息
     * @param inviterLevel        邀请者等级
     * @param inviterRewardType   奖励类型
     * @param inviterRewardAmount 奖励金额
     */
    public LockMarketRewardDetail getPreInviterLockMarketRewardDetail(
            LockMarketRewardDetail inviteeRewardDetail, LockMarketLevel inviteeLevel,
            Member inviterMember, LockMarketLevel inviterLevel,
            LockRewardType inviterRewardType, BigDecimal inviterRewardAmount) {
        LockMarketRewardDetail inviterRewardDetail = new LockMarketRewardDetail();
        //会员ID(推荐用户)
        inviterRewardDetail.setMemberId(inviterMember.getId());
        //推荐用户ID
        inviterRewardDetail.setInviterId(inviterMember.getInviterId());
        //奖励类型（直推奖、级差奖、培养奖）
        inviterRewardDetail.setRewardType(inviterRewardType);
        //奖励金额（不处理，由接收者处理）
        inviterRewardDetail.setRewardAmount(inviterRewardAmount);
        //会员当前等级
        inviterRewardDetail.setCurrentLevelId(inviterLevel.getMemberLevelId());
        //奖励率
        inviterRewardDetail.setCurrentRewardRate(inviterLevel.getRewardRate());
        //业绩数量
        inviterRewardDetail.setPerformanceTurnover(inviteeRewardDetail.getPerformanceTurnover());
        //总锁仓币数
        inviterRewardDetail.setTotalAmount(inviteeRewardDetail.getTotalAmount());
        //绩效系数
        inviterRewardDetail.setCurrentPerFactor(inviteeRewardDetail.getCurrentPerFactor());
        //关联的锁仓记录ID
        inviterRewardDetail.setLockDetailId(inviteeRewardDetail.getLockDetailId());
        //被推荐用户id
        inviterRewardDetail.setRefInviteeId(inviteeRewardDetail.getMemberId());
        //关联的锁仓用户ID
        inviterRewardDetail.setRefLockMemberId(inviteeRewardDetail.getRefLockMemberId());
        //锁仓天数
        inviterRewardDetail.setLockDays(inviteeRewardDetail.getLockDays());
        //锁仓时间
        inviterRewardDetail.setLockTime(inviteeRewardDetail.getLockTime());
        //记录状态
        inviterRewardDetail.setRecordStatus(ProcessStatus.NOT_PROCESSED);
        //业绩更新状态
        inviterRewardDetail.setPerUpdateStatus(ProcessStatus.NOT_PROCESSED);
        //等级更新状态
        inviterRewardDetail.setLevUpdateStatus(ProcessStatus.NOT_PROCESSED);
        //创建时间
        inviterRewardDetail.setCreateTime(new Date());
        //更新时间
        inviterRewardDetail.setUpdateTime(new Date());
        //币种
        inviterRewardDetail.setSymbol(inviteeRewardDetail.getSymbol());
        //inviterRewardDetail.setCode();     //记录校验码
        //inviterRewardDetail.setComment();  //备注

        //设置子部门中的最大奖励率
        inviterRewardDetail.setSubMaxRewardRate(
                inviteeLevel.getRewardRate().max(inviteeRewardDetail.getSubMaxRewardRate()));

        //培养奖出现次数
        if (inviterRewardType == LockRewardType.TRAINING) {
            inviterRewardDetail.setTrainingCount(inviteeRewardDetail.getTrainingCount() + 1);
        } else {
            inviterRewardDetail.setTrainingCount(inviteeRewardDetail.getTrainingCount());
        }

        return inviterRewardDetail;
    }

    /**
     * 推荐者的直推 市场奖励明细记录
     *
     * @param lockCoinDetail 锁仓明细记录
     * @param inviteeLevel   被推荐者等级
     * @param inviterMember  邀请者信息
     * @param inviterLevel   邀请者等级
     * @param perFactor      绩效系数
     * @return
     */
    public LockMarketRewardDetail getPreDirectInviterLockMarketRewardDetail(
            LockCoinDetail lockCoinDetail, LockMarketLevel inviteeLevel, Member inviterMember,
            LockMarketLevel inviterLevel, BigDecimal perFactor) {
        LockMarketRewardDetail inviterRewardDetail = new LockMarketRewardDetail();
        //会员ID(推荐用户)
        inviterRewardDetail.setMemberId(inviterMember.getId());
        //推荐用户ID
        inviterRewardDetail.setInviterId(inviterMember.getInviterId());
        //奖励类型（直推奖、级差奖、培养奖）
        inviterRewardDetail.setRewardType(LockRewardType.REFERRER);
        //奖励金额
        inviterRewardDetail.setRewardAmount(BigDecimal.ZERO);
        //会员当前等级(ref=LockMarketLevel.memberLevelId)
        inviterRewardDetail.setCurrentLevelId(inviterLevel.getMemberLevelId());
        //奖励率
        inviterRewardDetail.setCurrentRewardRate(inviterLevel.getRewardRate());
        //总锁仓币数
        inviterRewardDetail.setTotalAmount(lockCoinDetail.getTotalAmount());
        //绩效系数
        inviterRewardDetail.setCurrentPerFactor(perFactor);
        //业绩数量
        inviterRewardDetail.setPerformanceTurnover(lockCoinDetail.getTotalAmount().multiply(perFactor));
        //关联的锁仓记录ID
        inviterRewardDetail.setLockDetailId(lockCoinDetail.getId());
        //被推荐用户id
        inviterRewardDetail.setRefInviteeId(lockCoinDetail.getMemberId());
        //关联的锁仓用户ID
        inviterRewardDetail.setRefLockMemberId(lockCoinDetail.getMemberId());
        //锁仓天数
        inviterRewardDetail.setLockDays(lockCoinDetail.getLockDays());
        //锁仓时间
        inviterRewardDetail.setLockTime(lockCoinDetail.getLockTime());
        //记录状态
        inviterRewardDetail.setRecordStatus(ProcessStatus.NOT_PROCESSED);
        //业绩更新状态
        inviterRewardDetail.setPerUpdateStatus(ProcessStatus.NOT_PROCESSED);
        //等级更新状态
        inviterRewardDetail.setLevUpdateStatus(ProcessStatus.NOT_PROCESSED);
        ///inviterRewardDetail.setSubMaxRewardRate(inviteeLevel.getRewardRate());   //子部门中的最大奖励率
        //子部门中的最大奖励率
        inviterRewardDetail.setSubMaxRewardRate(BigDecimal.ZERO);
        //培养奖出现次数。注意：直推关系中出现培养奖也不计算培养奖的出现次数
        inviterRewardDetail.setTrainingCount(0);
        //创建时间
        inviterRewardDetail.setCreateTime(new Date());
        //更新时间
        inviterRewardDetail.setUpdateTime(new Date());
        //币种
        inviterRewardDetail.setSymbol(lockCoinDetail.getCoinUnit());
        //inviterRewardDetail.setCode();     //记录校验码
        //inviterRewardDetail.setComment();  //备注

        return inviterRewardDetail;
    }


    /**
     * 生成推送的消息
     *
     * @param inviteeRewardDetail 被邀请者 奖励明细记录
     * @param inviterRewardDetail 邀请人 奖励明细记录
     * @param type                消息类型
     * @param pervMessage         前一条消息，可以为null
     * @return
     */
    public CnytMarketRewardMessage getCnytMarketRewardMessage(LockMarketRewardDetail inviteeRewardDetail,
                                                              LockMarketRewardDetail inviterRewardDetail,
                                                              CnytMessageType type, CnytMarketRewardMessage pervMessage) {
        CnytMarketRewardMessage message = new CnytMarketRewardMessage();
        //消息类型
        message.setType(type);
        //被邀请者
        message.setInviteeMemberId(inviteeRewardDetail.getMemberId());
        //邀请者
        message.setInviterMemberId(inviterRewardDetail.getMemberId());
        //被邀请者 奖励明细记录ID
        message.setRefInviteeMarketRewardDetailId(inviteeRewardDetail.getId());
        //邀请者 奖励明细记录ID
        message.setRefInviterMarketRewardDetailId(inviterRewardDetail.getId());
        //已存在的最大奖励率
        message.setExistMaxRewardRate(inviterRewardDetail.getSubMaxRewardRate());
        //锁仓明细记录ID
        message.setRefLockDetatilId(inviteeRewardDetail.getLockDetailId());

        //记录推荐链
        if (pervMessage != null) {
            message.setAcyclicRecommendChain(pervMessage.getAcyclicRecommendChain());
        }
        if (type == CnytMessageType.PUSH_REWARD) {
            //锁仓用户
            message.getAcyclicRecommendChain().add(inviteeRewardDetail.getRefInviteeId().longValue());
            //发送消息时记录 锁仓用户的直接推荐人
            message.getAcyclicRecommendChain().add(inviteeRewardDetail.getMemberId().longValue());
        } else {
            //发送消息时记录 处理记录的推荐人
            message.getAcyclicRecommendChain().add(inviterRewardDetail.getMemberId().longValue());
        }

        //平级或越级处理状态
        if (inviterRewardDetail.getTrainingCount() <= 0) {
            message.setPeerStatus(CnytMessagePeerStatus.SLAVE);
        } else if (inviterRewardDetail.getTrainingCount() == 1) {
            message.setPeerStatus(CnytMessagePeerStatus.HINT_OVER_PEER);
        } else {
            message.setPeerStatus(CnytMessagePeerStatus.HINT_CULTIVATE_REWARDED);
        }

        return message;
    }

    /**
     * 生成推送的消息
     *
     * @param inviteeRewardDetail 被邀请者 奖励明细记录
     * @param type                消息类型
     * @return
     */
    public CnytMarketRewardMessage getCnytMarketRewardMessage(LockMarketRewardDetail inviteeRewardDetail,
                                                              CnytMessageType type) {
        CnytMarketRewardMessage message = new CnytMarketRewardMessage();
        //消息类型
        message.setType(type);
        //被邀请者
        message.setInviteeMemberId(inviteeRewardDetail.getMemberId());
        //邀请者
        message.setInviterMemberId(inviteeRewardDetail.getMemberId());
        //被邀请者 奖励明细记录ID
        message.setRefInviteeMarketRewardDetailId(inviteeRewardDetail.getId());
        //邀请者 奖励明细记录ID
        message.setRefInviterMarketRewardDetailId(inviteeRewardDetail.getId());
        //已存在的最大奖励率
        message.setExistMaxRewardRate(null);
        //平级或越级处理状态
        message.setPeerStatus(CnytMessagePeerStatus.SLAVE);
        //锁仓明细记录ID
        message.setRefLockDetatilId(inviteeRewardDetail.getLockDetailId());

        return message;
    }


}
