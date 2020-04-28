package com.spark.bitrade.service.cnyt;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.config.LockConfig;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockRewardSatus;
import com.spark.bitrade.constant.LockRewardType;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.mq.CnytMarketRewardMessage;
import com.spark.bitrade.mq.CnytMessageType;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *  * 锁仓市场奖励（直推奖、平级奖、极差奖）
 *  * @author tansitao
 *  * @time 2018/12/3 17:50 
 *  
 */
@Service
@Slf4j
public class LockMarketRewardService extends BaseService {

    @Autowired
    private LockMarketRewardDetailService lockMarketRewardDetailService;

    @Autowired
    private LockCoinDetailService lockCoinDetailService;

    @Autowired
    private LockMarketRewardIncomePlanService lockMarketRewardIncomePlanService;

    @Autowired
    private LockMarketPerformanceTotalService lockMarketPerformanceTotalService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LockMarketLevelService lockMarketLevelService;

    @Autowired
    private LockRewardLevelConfigService lockRewardLevelConfigService;

    @Autowired
    private InviterRewardService inviterRewardService;

    @Autowired
    private CompareLevelService compareLevelService;

    @Autowired
    private SendMessageService sendMessageService;

    @Autowired
    private LockRewardReturnService lockRewardReturnService;

    @Autowired
    private LockConfig lockConfig;

    private String getLogPrefix(CnytMarketRewardMessage message) {
        return String.format("LDId=%d,RDId=%d,memberId=%d ", message.getRefLockDetatilId(),
                message.getRefInviterMarketRewardDetailId(), message.getInviterMemberId());
    }

    /**
     *  * 处理cnyt增值计划消息，根据不同的消息类型进行不同的数据处理
     *  * @author tansitao
     *  * @time 2018/12/3 18:57 
     *  
     */
    @Async
    public void dealCNYTMessage(CnytMarketRewardMessage message) {
        /**
         * 1、判断消息类型
         * 2、处理对应类型的消息
         * 3、判断是否需要向外推出消息，如果需要则推出消息
         */
        CnytMessageType type = message.getType();
        switch (type) {
            //处理直推奖信息
            case PUSH_REWARD:
                dealPushReward(message);
                break;
            //处理极差奖信息
            case DIFFER_REWARD:
                dealDifferReward(message);
                break;
            //处理培养奖
            case CULTIVATE_REWARD:
                dealCultivateReward(message);
                break;
            //处理业绩
            case PERFORMANCE:
                dealPerformance(message);
                break;
            //处理等级
            case LEVEL:
                dealLevel(message);
                break;
            //实时返佣
            case REALTIME_REWARD:
                dealReward(message);
                break;
            default:
                log.warn("错误的消息类型，message={}", message);
        }
    }

    /**
     *  * 处理市场直推奖励
     *  * @author tansitao
     *  * @time 2018/12/3 17:59 
     *  
     */
    public void dealPushReward(CnytMarketRewardMessage message) {
        /**
         * 1、获取消息实体，对消息进行基础判断
         * 2、业务规则：直推奖=个人的新增业绩×自己等级的奖励率
         * 3、通过消息取出接收者id、奖励明细ID（当前）、奖励明细ID(上一级)，查询出数据
         * 4、判断要推出的消息是否平级，获取当前当前最高级别
         * 5、判断消息是否已经处理，如果没有处理则处理当前消息、更新市场奖励记录的状态为“已完成”
         * 6、判断上一级的数据是否已存在，如果不存在，生成上一级推荐人的级差奖励记录（仅保存关系数据，不计算业务数据）
         * 7、生成更新业绩消息、推出极差处理消息
         */

        //日志前缀
        String logPrefix = this.getLogPrefix(message);

        log.info("{}，===============处理直推消息：{}====================="
                , logPrefix, JSON.toJSONString(message));

        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //获取锁仓详情
///       LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(message.getRefLockDetatilId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            LockMarketRewardDetail inviterRewardDetail ;
            LockMarketLevel inviteeLevel = null, inviterLevel = null;

            //判断奖励明细是否已经处理
            if (lockMarketRewardDetail.getRecordStatus() == ProcessStatus.NOT_PROCESSED) {
                //计算 直推奖
                BigDecimal pushtReward = lockMarketRewardDetail.getPerformanceTurnover()
                        .multiply(lockMarketRewardDetail.getCurrentRewardRate()).setScale(8, BigDecimal.ROUND_DOWN);
                log.info("{}，计算直推奖={}", logPrefix, pushtReward);

                //判断 是否有推荐人
                if (StringUtils.isEmpty(lockMarketRewardDetail.getInviterId()) == false) {
                    //有推荐人 判断 是否平级越级
                    inviteeLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());
                    inviterLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getInviterId(), lockMarketRewardDetail.getSymbol());
                    if (compareLevelService.surpassInviter(inviteeLevel, inviterLevel)) {
                        //按 平级、越级 计算培养奖， 从 直推奖 中 扣除 培养奖
                        pushtReward = pushtReward.multiply(BigDecimal.ONE.subtract(lockConfig.getRate(lockMarketRewardDetail.getSymbol()))).setScale(8, BigDecimal.ROUND_DOWN);

                        log.info("{}，扣除培养奖的直推奖={}", logPrefix, pushtReward);
                    }
                }
                //设置 直推奖
                lockMarketRewardDetail.setRewardAmount(pushtReward);
                try {
                    //将奖励明细状态设置为处理中
                    boolean isUpdateSuccess = lockMarketRewardDetailService.updateRecordStatus(
                            lockMarketRewardDetail.getId(), ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED);
                    //如果状态更新成功开始处理返还奖金和推荐者的预处理奖励明细
                    if (isUpdateSuccess) {
                        inviterRewardDetail = getService().beginDealReward(lockMarketRewardDetail, inviteeLevel, inviterLevel, message);
                    } else {
                        log.warn("{}，直推奖励状态更新异常，不处理数据======================", logPrefix);
                        return;
                    }
                } catch (Exception e) {
                    log.error("{}，直推奖励处理异常，奖励记录信息={}", logPrefix, lockMarketRewardDetail);
                    log.error("=========直推奖励处理异常，异常信息", e);

                    //将奖励明细状态回退为已处理
                    lockMarketRewardDetailService.updateRecordStatus(lockMarketRewardDetail.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);

                    return;
                }
            } else {
                log.info("{}，直推奖励明细状态不是未处理，不进行处理", logPrefix);

                // 获取 inviterRewardDetail 数据
                inviterRewardDetail = lockMarketRewardDetailService.findOneByLockDetailAndMember(
                        lockMarketRewardDetail.getLockDetailId(), lockMarketRewardDetail.getInviterId());
            }
            //向下级推送消息
            sendNextLevelMessage(lockMarketRewardDetail, inviterRewardDetail, message);
        } else {
            log.warn("{}，直推奖励明细处理失败,无奖励明细", logPrefix);
        }

        log.info("{}，===============直推消息处理结束=====================", logPrefix);
    }

    /**
     *   处理极差奖励
     *
     * @author tansitao
     * @time 2018/12/3 18:20   
     */
    public void dealDifferReward(CnytMarketRewardMessage message) {

        /**
         * 1、获取消息实体，对消息进行基础判断
         * 2、级差奖=下属部门的新增业绩×（ 自己等级的奖励率－直推好友等级的奖励率）
         * 3、通过消息取出接收者id、奖励明细ID（当前）、奖励明细ID(上一级)，查询出数据
         * 4、判断要推出的消息是否平级，获取当前当前最高级别
         * 5、判断消息是否已经处理，如果没有处理则处理当前消息、更新市场奖励记录的状态为“已完成”
         * 6、判断上一级的数据是否已存在，如果不存在，生成上一级推荐人的级差奖励记录（仅保存关系数据，不计算业务数据）
         * 7、生成更新业绩消息、推出极差处理消息
         */

        //日志前缀
        String logPrefix = this.getLogPrefix(message);
        log.info("{}，===============处理极差消息：{}=====================", logPrefix, JSON.toJSONString(message));

        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //获取锁仓详情
///        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(message.getRefLockDetatilId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            LockMarketRewardDetail inviterRewardDetail = null;
            LockMarketLevel inviteeLevel = null, inviterLevel = null;
            //判断奖励明细是否已经处理
            if (lockMarketRewardDetail.getRecordStatus() == ProcessStatus.NOT_PROCESSED) {
                //开始处理奖励
                //获取极差
                BigDecimal differ = lockMarketRewardDetail.getCurrentRewardRate().subtract(message.getExistMaxRewardRate());
                if (differ.compareTo(BigDecimal.ZERO) >= 0) {
                    BigDecimal differReward = lockMarketRewardDetail.getPerformanceTurnover().multiply(differ).setScale(8, BigDecimal.ROUND_DOWN);

                    log.info("{}，计算极差奖={}", logPrefix, differReward);

                    //判断 是否有推荐人
                    if (StringUtils.isEmpty(lockMarketRewardDetail.getInviterId()) == false) {
                        //有推荐人 判断 是否平级越级
                        inviteeLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());
                        inviterLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getInviterId(), lockMarketRewardDetail.getSymbol());

                        //处理 第一次出现的培养奖
                        if (lockMarketRewardDetail.getTrainingCount() == 0
                                && compareLevelService.surpassInviter(inviteeLevel, inviterLevel)) {
                            //按 平级、越级 计算培养奖， 从 直推奖 中 扣除 培养奖
                            differReward = differReward.multiply(BigDecimal.ONE.subtract(lockConfig.getRate(lockMarketRewardDetail.getSymbol()))).setScale(8, BigDecimal.ROUND_DOWN);

                            log.info("{}，扣除培养奖之后的极差奖={}", logPrefix, differReward);
                        }
                    }
                    lockMarketRewardDetail.setRewardAmount(differReward);
                    try {
                        //将奖励明细状态设置为处理中
                        boolean isUpdateSuccess = lockMarketRewardDetailService.updateRecordStatus(
                                lockMarketRewardDetail.getId(), ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED);
                        //如果状态更新成功开始处理返还奖金和推荐者的预处理奖励明细
                        if (isUpdateSuccess) {
                            inviterRewardDetail = getService().beginDealReward(lockMarketRewardDetail, inviteeLevel, inviterLevel, message);
                        } else {
                            log.warn("{}，极差奖励状态更新异常，不处理数据======================", logPrefix);
                            return;
                        }
                    } catch (Exception e) {
                        log.error("{}，极差奖励处理异常，奖励记录信息={}", logPrefix, lockMarketRewardDetail);
                        log.error("=========极差奖励处理异常，异常信息", e);

                        //将奖励明细状态回退为已处理
                        lockMarketRewardDetailService.updateRecordStatus(lockMarketRewardDetail.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);
                        return;
                    }
                } else {
                    log.info("{}，极差奖励明细处理失败，{}用户当前等级{}和最大等级{}不是极差关系"
                            , logPrefix, lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getCurrentRewardRate(),
                            lockMarketRewardDetail.getSubMaxRewardRate());
                }
            } else {
                log.info("{}，极差奖励明细状态不是未处理，不进行处理", logPrefix);

                // 获取 inviterRewardDetail 数据
                inviterRewardDetail = lockMarketRewardDetailService.findOneByLockDetailAndMember(
                        lockMarketRewardDetail.getLockDetailId(), lockMarketRewardDetail.getInviterId());
            }
            //向下级推送
            sendNextLevelMessage(lockMarketRewardDetail, inviterRewardDetail, message);

        } else {
            log.warn("{}，极差奖励明细处理失败,无奖励明细", logPrefix);
        }

        log.info("{}，===============极差消息处理结束=====================", logPrefix);
    }

    /**
     *  处理培养奖励
     *  @author tansitao
     *  @time 2018/12/3 18:20 
     *  
     */
    public void dealCultivateReward(CnytMarketRewardMessage message) {

        /**
         * 1、获取消息实体，对消息进行基础判断
         * 2、直推好友超过推荐人级别或平级的情况， 产生新业绩时， 提取被推荐人收益的10%将给到推荐人作为培养奖励。
         * 3、通过消息取出接收者id、奖励明细ID（当前）、奖励明细ID(上一级)，查询出数据
         * 4、判断要推出的消息是否平级，获取当前当前最高级别
         * 5、判断消息是否已经处理，如果没有处理则处理当前消息、更新市场奖励记录的状态为“已完成”
         * 6、判断上一级的数据是否已存在，如果不存在，生成上一级推荐人的级差奖励记录（仅保存关系数据，不计算业务数据）
         * 7、生成更新业绩消息、推出极差处理消息
         */

        //日志前缀
        String logPrefix = this.getLogPrefix(message);
        log.info("{}，===============处理培养奖励消息：{}=====================", logPrefix, JSON.toJSONString(message));

        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //获取锁仓详情
///        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(message.getRefLockDetatilId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            LockMarketRewardDetail inviterRewardDetail;
            LockMarketLevel inviteeLevel = null, inviterLevel = null;
            //判断奖励明细是否已经处理
            if (lockMarketRewardDetail.getRecordStatus() == ProcessStatus.NOT_PROCESSED) {
                //判断 是否有推荐人
                if (StringUtils.isEmpty(lockMarketRewardDetail.getInviterId()) == false) {
                    //有推荐人 判断 是否平级越级
                    inviteeLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());
                    inviterLevel = lockMarketLevelService.findByMemberId(lockMarketRewardDetail.getInviterId(), lockMarketRewardDetail.getSymbol());
                }
                //开始处理奖励，只有状态为平级越级的时候才处理
//                if(message.getPeerStatus().equals(CnytMessagePeerStatus.HINT_OVER_PEER)){
//                    log.info("=============培养奖：{}============",lockMarketRewardDetail.getRewardAmount());
//                }else {
//                    log.info("================{}培养奖已被领取，不处理该培养奖{}======================", message.getRefInviterMarketRewardDetailId(), lockMarketRewardDetail.getRewardAmount());
//                }

                try {
                    //将奖励明细状态设置为处理中
                    boolean isUpdateSuccess = lockMarketRewardDetailService.updateRecordStatus(lockMarketRewardDetail.getId(),
                            ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED);
                    //如果状态更新成功开始处理返还奖金和推荐者的预处理奖励明细
                    if (isUpdateSuccess) {
                        inviterRewardDetail = getService().beginDealReward(lockMarketRewardDetail, inviteeLevel, inviterLevel, message);
                    } else {
                        log.warn("{}，培养奖励状态更新异常，不处理数据======================", logPrefix);
                        return;
                    }
                } catch (Exception e) {
                    log.error("{}，培养奖励处理异常，奖励记录信息={}", logPrefix, lockMarketRewardDetail);
                    log.error("=========培养奖励处理异常，异常信息", e);

                    //将奖励明细状态回退为已处理
                    lockMarketRewardDetailService.updateRecordStatus(lockMarketRewardDetail.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);
                    return;
                }

            } else {
                log.info("{}，培养奖励明细状态不是未处理，不进行处理", logPrefix);

                // 获取 inviterRewardDetail 数据
                inviterRewardDetail = lockMarketRewardDetailService.findOneByLockDetailAndMember(
                        lockMarketRewardDetail.getLockDetailId(), lockMarketRewardDetail.getInviterId());
            }
            //向下级推送
            sendNextLevelMessage(lockMarketRewardDetail, inviterRewardDetail, message);

        } else {
            log.warn("{}，培养奖励明细处理失败,无奖励明细", logPrefix);
        }

        log.info("{}，===============培养奖消息处理结束=====================", logPrefix);
    }

    /**
     *  * 处理会员业绩
     *  * @author tansitao
     *  * @time 2018/12/3 18:38 
     *  
     */
    public void dealPerformance(CnytMarketRewardMessage message) {
        /**
         *  1、幂等性，判断“市场奖励明细表.业绩更新状态”
         *  2、更新子部门的累计业绩（表：会员市场奖励业绩总累计表）
         *  3、更新“市场奖励明细表.业绩更新状态”为“已处理”
         *  4、发送通知：更新等级
         */
        //日志前缀
        String logPrefix = this.getLogPrefix(message);

        log.info("{}，===============处理会员业绩：{}=====================", logPrefix, JSON.toJSONString(message));
        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //获取锁仓详情
///        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(message.getRefLockDetatilId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            //判断业绩状态是否已经处理
            if (lockMarketRewardDetail.getPerUpdateStatus() == ProcessStatus.NOT_PROCESSED) {
                lockMarketRewardDetail.setPerUpdateStatus(ProcessStatus.PROCESSED);
                //获取会员业绩数据
                LockMarketPerformanceTotal lockMarketPerformanceTotal =
                        lockMarketPerformanceTotalService.findByMemberId(lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());

                try {
                    //将奖励明细业绩状态设置为处理中
                    boolean isUpdateSuccess = lockMarketRewardDetailService.updatePerUpdateStatus(
                            lockMarketRewardDetail.getId(), ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED);
                    if (isUpdateSuccess) {
                        getService().updatePerformance(lockMarketRewardDetail, lockMarketPerformanceTotal);
                    } else {
                        log.warn("{}，业绩状态更新异常，不处理数据", logPrefix);
                        return;
                    }
                } catch (Exception e) {
                    log.error("{}，业绩处理异常，奖励记录信息={}", logPrefix, lockMarketRewardDetail);
                    log.error("=========业绩处理异常，异常信息", e);

                    lockMarketRewardDetailService.updatePerUpdateStatus(lockMarketRewardDetail.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);
                }

            } else {
                log.info("{}，业绩状态不是未处理，不进行处理", logPrefix);
            }

            //推送更新等级消息
            sendMessageService.sendCnytUpdateMessage(lockMarketRewardDetail, CnytMessageType.LEVEL);
        } else {
            log.warn("{}，业绩状态处理失败,无奖励明细", logPrefix);
        }
        log.info("{}，===============业绩消息处理结束=====================", logPrefix);
    }

    /**
     *  * 处理会员等级
     *  * @author tansitao
     *  * @time 2018/12/3 18:38 
     *  
     */
    public void dealLevel(CnytMarketRewardMessage message) {
        /**
         * 1、幂等性，判断“市场奖励明细表.等级更新状态”
         * 2、满足升级条件则更新等级（表：会员STO市场等级表）
         * 3、更新“市场奖励明细表.等级更新状态”为“已处理”
         */
        //日志前缀
        String logPrefix = this.getLogPrefix(message);

        log.info("{}，===============处理会员等级：{}=====================", logPrefix, JSON.toJSONString(message));
        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //获取锁仓详情
///        LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(message.getRefLockDetatilId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            //判断等级状态是否已经处理
            if (lockMarketRewardDetail.getLevUpdateStatus() == ProcessStatus.NOT_PROCESSED) {
                lockMarketRewardDetail.setLevUpdateStatus(ProcessStatus.PROCESSED);
                //查询会员市场等级
                LockMarketLevel lockMarketLevel = lockMarketLevelService.findByMemberId(
                        lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());
                log.info("{}，查询会员市场等级={}", logPrefix, lockMarketLevel);

                //查询极差配置信息
                List<LockRewardLevelConfig> lstLevel =
                        lockRewardLevelConfigService.getLevelConfigList(lockMarketRewardDetail.getSymbol());
                log.info("{}，查询极差配置信息={}", logPrefix, lstLevel);

                //查询当前等级配置信息
                LockRewardLevelConfig currLevel = lockRewardLevelConfigService.getLevelConfigById(
                        lockMarketLevel.getMemberLevelId().intValue(),lockMarketRewardDetail.getSymbol());
                log.info("{}，查询当前等级配置信息={}", logPrefix, currLevel);

                //查询会员所有子部门数据
                List<LockMarketPerformanceTotal> lstPerformance =
                        lockMarketPerformanceTotalService.findAllByInivite(
                                lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getSymbol());
                log.info("{}，查询会员所有子部门数据={}", logPrefix, lstPerformance);

                //比较生成目标等级
                LockRewardLevelConfig targetLevel = compareLevelService.matchLevel(lstLevel, currLevel, lstPerformance);
                log.info("{}，比较生成目标等级={}", logPrefix, targetLevel);

                try {
                    //将奖励明细业绩状态设置为处理中
                    boolean isUpdateSuccess = lockMarketRewardDetailService.updateLevUpdateStatus(
                            lockMarketRewardDetail.getId(), ProcessStatus.PROCESSING, ProcessStatus.NOT_PROCESSED);
                    if (isUpdateSuccess) {
                        getService().updateLockMarketLevel(lockMarketRewardDetail, lockMarketLevel, currLevel, targetLevel, message);
                    } else {
                        log.warn("{}，会员等级状态更新异常，不处理数据", logPrefix);
                        return;
                    }
                } catch (Exception e) {
                    log.error("{}，会员等级处理异常，奖励记录信息={}", logPrefix, lockMarketRewardDetail);
                    log.error("=========会员等级处理异常，异常信息", e);

                    //回滚状态
                    lockMarketRewardDetailService.updateLevUpdateStatus(
                            lockMarketRewardDetail.getId(), ProcessStatus.NOT_PROCESSED, ProcessStatus.PROCESSING);
                }

            } else {
                log.info("{}，等级状态不是未处理，不进行处理", logPrefix);
            }
        } else {
            log.warn("{}，等级状态处理失败,无奖励明细", logPrefix);
        }
        log.info("{}，===============等级消息处理结束=====================", logPrefix);
    }

    /**
     *  * 处理实时返佣
     *  * @author tansitao
     *  * @time 2018/12/6 17:44 
     *  
     */
    public void dealReward(CnytMarketRewardMessage message) {
        //日志前缀
        String logPrefix = this.getLogPrefix(message);

        log.info("{}，===============处理实时返佣：{}=====================", logPrefix, JSON.toJSONString(message));
        //获取市场奖励明细
        LockMarketRewardDetail lockMarketRewardDetail = lockMarketRewardDetailService.findOneById(message.getRefInviterMarketRewardDetailId());
        //判断收益明细和锁仓记录是否都存在，只要有一个不存在则不处理
        if (lockMarketRewardDetail != null) {
            //获取锁仓详情
            LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockMarketRewardDetail.getLockDetailId());
            LockMarketRewardIncomePlan lockMarketRewardIncomePlan = lockMarketRewardIncomePlanService.findOneByDetailIdAndMemberId(lockMarketRewardDetail.getId(),
                    lockMarketRewardDetail.getMemberId(), DateUtil.dateToString(DateUtil.addMinToDate(new Date(), 5)), LockBackStatus.BACK);
            if (lockMarketRewardIncomePlan != null) {
                lockRewardReturnService.returnRewardIncome(lockMarketRewardIncomePlan, lockCoinDetail);
            } else {
                log.info("{}，实时返佣处理失败,无返还记录================", logPrefix);
            }

        } else {
            log.info("{}，实时返佣处理失败,无奖励明细=====", logPrefix);
        }

        log.info("{}，===============处理实时返佣结束=====================", logPrefix);
    }

    /**
     *  * 修改奖励明细，生成奖励返还计划
     *  * @author tansitao
     *  * @time 2018/12/4 14:46 
     *  
     */
    @Transactional(rollbackFor = Exception.class)
    public LockMarketRewardDetail beginDealReward(LockMarketRewardDetail lockMarketRewardDetail,
                                                  LockMarketLevel inviteeLevel, LockMarketLevel inviterLevel, CnytMarketRewardMessage pervMessage) {
        //日志前缀
        String logPrefix = this.getLogPrefix(pervMessage);

        log.info("{}，生成用户{}的市场奖励返佣计划", logPrefix, lockMarketRewardDetail.getMemberId());

        boolean levelCheck = lockCoinDetailService.checkMemberRewardQualification(lockMarketRewardDetail, lockMarketRewardDetail.getSymbol());

        //edit by yangch 时间： 2019.03.20 原因：调用新等级判断结果
        if (levelCheck) {
            //返佣
            BigDecimal rewardAmount = lockMarketRewardDetail.getRewardAmount();
            if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
                LockMarketRewardIncomePlan lmrip = new LockMarketRewardIncomePlan();
                lmrip.setRewardAmount(rewardAmount);
                lmrip.setLockDetailId(lockMarketRewardDetail.getLockDetailId());
                lmrip.setMarketRewardDetailId(lockMarketRewardDetail.getId());
                lmrip.setMemberId(lockMarketRewardDetail.getMemberId());
                lmrip.setPeriod(1);
                lmrip.setRewardTime(lockMarketRewardDetail.getLockTime());
                lmrip.setStatus(LockBackStatus.BACK);
                lmrip.setRewardType(lockMarketRewardDetail.getRewardType());
                lmrip.setSymbol(lockMarketRewardDetail.getSymbol());

                //保存返佣记录实时发放奖励
                LockMarketRewardIncomePlan newIncomePlan = lockMarketRewardIncomePlanService.saveToReturn(lmrip);
                //获取锁仓详情
                LockCoinDetail lockCoinDetail = lockCoinDetailService.findOne(lockMarketRewardDetail.getLockDetailId());
                lockRewardReturnService.returnRewardIncome(newIncomePlan, lockCoinDetail);
            } else {
                log.info("{}，用户{}的市场奖励返佣为零，不保存场奖励返佣计划", logPrefix, lockMarketRewardDetail.getMemberId());
            }


//            //获取返佣周期，返佣周期大于0则进行，则生成返还计划
//            int cycles = lockMarketRewardDetail.getLockDays() / lockConfig.getCycle();
//            //计算每期收益
//            if (cycles > 0) {
//                BigDecimal rewardAmount = lockMarketRewardDetail.getRewardAmount().divide(BigDecimal.valueOf(cycles), 8, BigDecimal.ROUND_DOWN);
//                //根据期数生成，多期返还的记录
//                for (int i = 0; rewardAmount.compareTo(BigDecimal.ZERO) > 0 && i < cycles; i++) {
//                    LockMarketRewardIncomePlan lmrip = new LockMarketRewardIncomePlan();
//                    //判断是不是最后一期
//                    if (i == cycles - 1) {
//                        //如果是最后一期，用总的返佣-前几期的范返佣之和
//                        lmrip.setRewardAmount(lockMarketRewardDetail.getRewardAmount().subtract(rewardAmount.multiply(BigDecimal.valueOf(i))));
//                    } else {
//                        lmrip.setRewardAmount(rewardAmount);
//                    }
//                    lmrip.setLockDetailId(lockMarketRewardDetail.getLockDetailId());
//                    lmrip.setMarketRewardDetailId(lockMarketRewardDetail.getId());
//                    lmrip.setMemberId(lockMarketRewardDetail.getMemberId());
//                    lmrip.setPeriod(i + 1);
//                    lmrip.setRewardTime(DateUtil.addDay(lockMarketRewardDetail.getLockTime(), i * lockConfig.getCycle()));
//                    lmrip.setStatus(LockBackStatus.BACK);
//                    lmrip.setRewardType(lockMarketRewardDetail.getRewardType());
//
//                    lockMarketRewardIncomePlanService.save(lmrip);
//                }
//            } else {
//                log.warn("{}，返佣收益分期数小于0，LockDays={}，PERIOD={}", logPrefix
//                        , lockMarketRewardDetail.getLockDays(), lockConfig.getCycle());
//            }
        } else {
            //新等级不满足，不发奖励，重新赋值奖励为0
            lockMarketRewardDetail.setRewardAmount(BigDecimal.ZERO);
        }
        //保存当前用户处理了数据的奖励明细记录
        lockMarketRewardDetail.setRecordStatus(ProcessStatus.PROCESSED);
        lockMarketRewardDetailService.save(lockMarketRewardDetail);

        log.info("{}，预处理推荐人的市场奖励明细记录，inviteeId={}", logPrefix, lockMarketRewardDetail.getMemberId());

        //判断 是否有 推荐人
        if (StringUtils.isEmpty(lockMarketRewardDetail.getInviterId()) == false) {

            //判断推荐人链中是否存在循环关系，存在循环关系则退出推荐关系的递推
            log.warn("{}，判断推荐人关系中是否存在循环关系，已存在的推荐链={}，下一推荐人={}", logPrefix,
                    pervMessage.getAcyclicRecommendChain(), lockMarketRewardDetail.getInviterId());

            if (pervMessage.getAcyclicRecommendChain().contains(lockMarketRewardDetail.getInviterId().longValue())) {
                log.warn("{}，推荐人关系中存在循环关系，inviteeId={}，inviterId={}，recommendChain={}", logPrefix,
                        lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getId(), pervMessage.getAcyclicRecommendChain());
                return null;
            }

            Member inviterMember = memberService.findOne(lockMarketRewardDetail.getInviterId());
            if (StringUtils.isEmpty(inviterMember) == false) {
                log.warn("{}，当前用户和推荐者的等级比较，inviterLevel={}，inviteeLevel={}",
                        logPrefix, inviterLevel, inviteeLevel);
                //判断当前用户和推荐者的等级
                if (inviterLevel.getRewardRate().compareTo(
                        inviteeLevel.getRewardRate().max(lockMarketRewardDetail.getSubMaxRewardRate())) > 0
                        //当推荐关系中用户的等级都为0（虚拟节点）时，识别为“级差奖”的场景
                        || (inviterLevel.getMemberLevelId().intValue() ==0
                        && inviteeLevel.getMemberLevelId().intValue() ==0)) {

                    log.info("{}，预处理差奖励，inviteeId={}，inviterId={}", logPrefix,
                            lockMarketRewardDetail.getMemberId(), inviterMember.getId());

                    //如果小于0则进行极差奖励预处理
                    LockMarketRewardDetail preLockMarketRewardDetail =
                            inviterRewardService.getPreInviterLockMarketRewardDetail(
                                    lockMarketRewardDetail, inviteeLevel, inviterMember,
                                    inviterLevel, LockRewardType.CROSS, BigDecimal.ZERO);

                    return lockMarketRewardDetailService.save(preLockMarketRewardDetail);
                } else {
                    //如果不小于零，则是培养奖明细预处理
                    //计算 培养奖 获得奖励/（1-培养奖率）
                    BigDecimal trainingReward = BigDecimal.ZERO;
                    if (lockMarketRewardDetail.getTrainingCount() < 1) {
                        //奖金/(1-0.1)-奖金
                        trainingReward = lockMarketRewardDetail.getRewardAmount()
                                .divide(BigDecimal.ONE.subtract(lockConfig.getRate(lockMarketRewardDetail.getSymbol())), 8, BigDecimal.ROUND_DOWN).
                                        subtract(lockMarketRewardDetail.getRewardAmount());
                    }

                    log.info("{}，预处理培养奖={}，inviteeId={}，inviterId={}", logPrefix, trainingReward,
                            lockMarketRewardDetail.getMemberId(), inviterMember.getId());

                    LockMarketRewardDetail preLockMarketRewardDetail =
                            inviterRewardService.getPreInviterLockMarketRewardDetail(
                                    lockMarketRewardDetail, inviteeLevel, inviterMember,
                                    inviterLevel, LockRewardType.TRAINING, trainingReward);

                    return lockMarketRewardDetailService.save(preLockMarketRewardDetail);
                }
            } else {
                log.info("{}，用户{}没有推荐人{}不存在", logPrefix
                        , lockMarketRewardDetail.getMemberId(), lockMarketRewardDetail.getInviterId());
            }
        } else {
            log.info("{}，用户{}的推荐人不存在，退出市场奖励的处理", logPrefix
                    , lockMarketRewardDetail.getMemberId());
        }
        return null;
    }

    /**
     *  * 更新等级
     *  * @author tansitao
     *  * @time 2018/12/5 16:56 
     *  
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLockMarketLevel(LockMarketRewardDetail lockMarketRewardDetail, LockMarketLevel lockMarketLevel,
                                      LockRewardLevelConfig currLevel, LockRewardLevelConfig targetLevel, CnytMarketRewardMessage pervMessage) {
        //日志前缀
        String logPrefix = this.getLogPrefix(pervMessage);

        //保存当前用户处理了数据的奖励明细记录
        lockMarketRewardDetailService.save(lockMarketRewardDetail);

        //判断是否需要更新当前等级到目标等级
        if (compareLevelService.canUpgradeLevel(currLevel, targetLevel)) {
            log.info("{}，更新会员等级,currLevel={},targetLevel={}", logPrefix, currLevel, targetLevel);
            //更新会员等级
            lockMarketLevel.setLevel(targetLevel.getLevel());
            lockMarketLevel.setMemberLevelId(Long.valueOf(targetLevel.getLevelId()));
            lockMarketLevel.setRewardRate(targetLevel.getRewardRate());
            lockMarketLevel.setUpdateTime(new Date());
            lockMarketLevelService.save(lockMarketLevel);
        }
    }

    /**
     *  * 更新业绩
     *  * @author tansitao
     *  * @time 2018/12/5 16:56 
     *  
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePerformance(LockMarketRewardDetail lockMarketRewardDetail, LockMarketPerformanceTotal lockMarketPerformanceTotal) {
        //保存当前用户处理了数据的奖励明细记录
        lockMarketRewardDetailService.save(lockMarketRewardDetail);

        if (lockMarketPerformanceTotal != null) {
            //更新业绩
///            lockMarketPerformanceTotal.setSubDepartmentAmountTotal(lockMarketPerformanceTotal.getSubDepartmentAmountTotal().add(lockMarketRewardDetail.getPerformanceTurnover()));
            lockMarketPerformanceTotal.setIniviteId(lockMarketPerformanceTotal.getIniviteId());
            lockMarketPerformanceTotal.setUpdateTime(new Date());
            //保存业绩
            lockMarketPerformanceTotalService.save(lockMarketPerformanceTotal);
            //用sql更新业绩
            lockMarketPerformanceTotalService.updataPerformance(lockMarketRewardDetail.getPerformanceTurnover(), lockMarketRewardDetail.getMemberId());

        } else {
            lockMarketPerformanceTotal = new LockMarketPerformanceTotal();
            lockMarketPerformanceTotal.setIniviteId(lockMarketRewardDetail.getInviterId());
            lockMarketPerformanceTotal.setSubDepartmentAmountTotal(lockMarketRewardDetail.getPerformanceTurnover());
            lockMarketPerformanceTotal.setMemberId(lockMarketRewardDetail.getMemberId());
            lockMarketPerformanceTotal.setUpdateTime(new Date());
            lockMarketPerformanceTotal.setSymbol(lockMarketRewardDetail.getSymbol());
            //保存业绩
            lockMarketPerformanceTotalService.save(lockMarketPerformanceTotal);
        }

    }

    /**
     *  * 推送消息到下一级
     *  * @author tansitao
     *  * @time 2018/12/6 11:35 
     *  
     */
    public void sendNextLevelMessage(LockMarketRewardDetail lockMarketRewardDetail,
                                     LockMarketRewardDetail inviterRewardDetail, CnytMarketRewardMessage pervMessage) {
        //日志前缀
        String logPrefix = this.getLogPrefix(pervMessage);

        //向下级推送
        if (inviterRewardDetail != null) {
            log.info("{}，推送处理推荐人市场奖励的通知,inviteeId={}，inviterId={}", logPrefix
                    , lockMarketRewardDetail.getMemberId(), inviterRewardDetail.getMemberId());

            if (inviterRewardDetail.getRewardType() == LockRewardType.CROSS) {
                sendMessageService.sendCnytMessage(lockMarketRewardDetail, inviterRewardDetail, CnytMessageType.DIFFER_REWARD, pervMessage);
            } else if (inviterRewardDetail.getRewardType() == LockRewardType.TRAINING) {
                sendMessageService.sendCnytMessage(lockMarketRewardDetail, inviterRewardDetail, CnytMessageType.CULTIVATE_REWARD, pervMessage);
            } else {
                log.warn("{}，预处理奖励明细的返佣类型错误", logPrefix);
            }
        } else {
            log.info("{}，用户{}没有推荐关系，不需要向外推送", logPrefix
                    , lockMarketRewardDetail.getMemberId());

            //结束推荐关系时 更新 锁仓记录的 返佣状态
            lockCoinDetailService.updateLockRewardSatus(pervMessage.getRefLockDetatilId(),
                    LockRewardSatus.NO_REWARD, LockRewardSatus.ALREADY_REWARD);
        }

        log.info("{}，推送更新业绩信息的通知", logPrefix);
        //推送更新业绩信息
        sendMessageService.sendCnytUpdateMessage(lockMarketRewardDetail, CnytMessageType.PERFORMANCE);

        //推送实时返佣信息
        if (lockMarketRewardDetail.getRewardAmount().compareTo(BigDecimal.ZERO) > 0 || lockMarketRewardDetail.getTrainingCount() <= 1) {
            log.info("{}，推送实时返佣信息的通知", logPrefix);
            //推送实时返佣信息
            sendMessageService.sendCnytUpdateMessage(lockMarketRewardDetail, CnytMessageType.REALTIME_REWARD);
        }
    }

    public LockMarketRewardService getService() {
        return SpringContextUtil.getBean(LockMarketRewardService.class);
    }
}
