package com.spark.bitrade.jobhandler.promote;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.service.BettingConfigService;
import com.spark.bitrade.service.BettingStateService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.xxl.job.core.log.XxlJobLogger.log;

/***
 * 竞猜活动定时任务
 * @author yangch
 * @time 2018.09.14 14:56
 */
@JobHandler(value="PromoteGuestJobHandler")
@Component
public class PromoteGuestJobHandler extends IJobHandler {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //实现思路：
        // 1、获取所有有效的活动(假如有多种竞猜币种活动)
        // 2、判断活动是否在生效中
        // 3、判断当前时间在活动的某个时间区域，判断状态是否为阶段的状态（状态不对则更新状态）
        // 4、判断“分配标记表”中是否有相应的记录，及状态是否为完成；不为完成状态，则发送kafka消息

        ReturnT<String> returnT = new ReturnT<>(ReturnT.SUCCESS_CODE,"执行成功");
        log("-----竞猜活动定时任务-----begin");

        //1、获取所有有效的活动
        List<BettingConfig> list =  bettingConfigService.findAllOfLately();
        if(null != list) {
            log("--当前有效的活动数 =" +list.size());
            Date currentDate = getCurrentDate();
            log("系统当前时间：{0}", currentDate);

            list.stream().forEach(bettingConfig->{
                if(bettingConfig.getStatus() == BettingConfigStatus.STAGE_INVALID
                        || bettingConfig.getStatus() == BettingConfigStatus.STAGE_FINISHED
                        || bettingConfig.getDeleted() == BooleanEnum.IS_TRUE ){
                    log("--当前活动的状态：{0}，数据是否删除：{1}",
                            bettingConfig.getStatus().getNameCn(), bettingConfig.getDeleted().getNameCn());
                } else {
                    //2、正在运行的活动，活动状态参考：①.未生效；②.未开始；③投票中；④、待开奖；⑤.领奖中；⑥.已完成
                    if(dateCompareTo(currentDate, bettingConfig.getBeginTime()) >=0
                            && dateCompareTo(currentDate, bettingConfig.getEndTime()) <=0){
                        //2.1、当前时间 在'投注开始时间'和 '投注结束时间'范围，则归为 ‘投票阶段’
                        this.stageVoting(bettingConfig);
                    } else if(dateCompareTo(currentDate, bettingConfig.getEndTime()) > 0
                            && dateCompareTo(currentDate, bettingConfig.getOpenTime()) < 0){
                        //2.2、当前时间 在'投注结束时间' 和 '开奖时间' 范围，则归为 ‘待开奖阶段’
                        this.stageWaiting(bettingConfig);
                    } else if(dateCompareTo(currentDate, bettingConfig.getOpenTime()) > 0 ){
                        //2.3、当前时间 大于 开奖时间 ，则进行开奖操作，并激活 领奖和抢红包
                        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_PRIZING) {
                            // 开奖 ，开奖后状态更新为'领奖中'状态
                            this.determineGuessPrice(bettingConfig);
                        } else {
                            //开奖后中奖处理和消息推送
                            this.determineGuessPriceAfter(bettingConfig);

                            if(dateCompareTo(currentDate, bettingConfig.getPrizeBeginTime()) >=0
                                    && dateCompareTo(currentDate, bettingConfig.getPrizeEndTime()) <=0) {
                                //领奖阶段
                                this.stagePrizing(bettingConfig);
                            } else if(dateCompareTo(currentDate, bettingConfig.getPrizeEndTime()) >0){
                                //领奖结束
                                this.stagePrizingEnd(bettingConfig);
                            }

                            if(dateCompareTo(currentDate, bettingConfig.getRedpacketBeginTime()) >=0
                                    && dateCompareTo(currentDate, bettingConfig.getRedpacketEndTime()) <=0) {
                                //抢红包阶段
                                this.stageRedpacket(bettingConfig);
                            } else if(dateCompareTo(currentDate, bettingConfig.getRedpacketEndTime()) >0){
                                //红包结束
                                this.stageRedpacketEnd(bettingConfig);
                            }
                        }

                        //判断活动是否结束
                        if(dateCompareTo(currentDate, bettingConfig.getPrizeEndTime()) >0
                                && dateCompareTo(currentDate, bettingConfig.getRedpacketEndTime()) >0){
                            //2.4、当前时间 大于 ‘领奖结束时间’ 和 ‘红包领取结束时间’，则归为 活动完成
                            this.stageFinished(bettingConfig);
                        }
                    }
                }
            });
        } else {
            log("--当前没有有效的活动--");
        }

        log("-----竞猜活动定时任务-----end");
        return returnT;
    }

    private int dateCompareTo(Date date1 , Date date2) {
        log("date1={0},date2={1},datetime1={2}, datetime2={3}", date1, date2, date1.getTime(), date2.getTime());

        int i = date1.compareTo(date2);
        if(i>0 || date1.getTime()> date2.getTime()) {
            return 1;
        } else if(i<0 || date1.getTime() < date2.getTime()) {
            return -1;
        }

        return 0;
    }


    //获取系统当前时间
    private Date getCurrentDate(){
        return new Date();
    }

    //发送kafka消息
    private void sendKafkaMessage(BettingState bettingState){
        if(null != bettingState
                && bettingState.getMark() != BettingStateOperateMark.TREATED) {
            kafkaTemplate.send(GuessKafkaTopicConstant.TX_PROMOTE_GUESS_HANDLER,
                    String.valueOf(bettingState.getOperate().getCode()),
                    JSON.toJSONString(bettingState));
        }
    }



    //投票阶段
    private void stageVoting(BettingConfig bettingConfig){
        //投票阶段，更新状态为‘投票中’
        log("--投票阶段，投注时间段：{0} 至 {1}",
                bettingConfig.getBeginTime(), bettingConfig.getEndTime());

        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_VOTING){
            log("--更新状态为‘投票中’---");
            bettingConfigService.asnycUpdateById(bettingConfig.getId(),
                    BettingConfigStatus.STAGE_VOTING);
        }
    }

    //待开奖阶段
    private void stageWaiting(BettingConfig bettingConfig){
        //待开奖阶段，1）更新状态为‘待开奖’；2）发送‘分红’任务；3）发送‘回购’任务
        log("--待开奖阶段，投票截止时间：{0}，开奖时间：{1}",
                bettingConfig.getEndTime(), bettingConfig.getOpenTime());

        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_WAITING){
            log("--更新状态为‘待开奖’---");
            bettingConfigService.asnycUpdateById(bettingConfig.getId(),
                    BettingConfigStatus.STAGE_WAITING);
        }

        //推送‘分红’任务消息
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_REWARD));

        //推送‘回购’任务消息
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_BUY_BACK));

        //推送‘红包扣除’任务消息
        BettingState stateRedpacket =bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_REDPACKET);
        if(stateRedpacket.getMark() != BettingStateOperateMark.TREATED) {
            this.sendKafkaMessage(stateRedpacket);
        } else {
            //推送‘红包准备’任务消息（等‘红包扣除’完成后再准备红包）
            this.sendKafkaMessage(bettingStateService.findBettingState(
                    bettingConfig.getId(), BettingStateOperateType.OP_REDPACKET_READY));
        }
    }

    //开奖
    private void determineGuessPrice(BettingConfig bettingConfig){
        // 开奖 ，开奖后状态更新为'领奖中'状态
        log("--开奖，开奖时间：{0}", bettingConfig.getOpenTime());
        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_PRIZING){
            //开奖通知
            BettingState stateMarkGuessPrice =bettingStateService.findBettingState(
                    bettingConfig.getId(), BettingStateOperateType.OP_MARK_GUESS_PRICE);
            if(stateMarkGuessPrice.getMark() != BettingStateOperateMark.TREATED) {
                this.sendKafkaMessage(stateMarkGuessPrice);
            }/* else {
                //中奖奖励操作通知
                this.sendKafkaMessage(bettingStateService.findBettingState(
                        bettingConfig.getId(), BettingStateOperateType.OP_PRAISE));

                //中奖消息推送 通知
                this.sendKafkaMessage(bettingStateService.findBettingState(
                        bettingConfig.getId(), BettingStateOperateType.OP_PRAISE_NOTIFICATION));
            }*/
        }
    }
    //开奖后中奖处理和消息推送
    private void determineGuessPriceAfter(BettingConfig bettingConfig){
        // 开奖 ，开奖后状态更新为'领奖中'状态
        log("--中奖处理和消息推送，开奖时间：{0}", bettingConfig.getOpenTime());
        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_PRIZING){
            //中奖奖励操作通知
            this.sendKafkaMessage(bettingStateService.findBettingState(
                    bettingConfig.getId(), BettingStateOperateType.OP_PRAISE));
        }

        //中奖消息推送 通知
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_PRAISE_NOTIFICATION));
    }

    //领奖阶段
    private void stagePrizing(BettingConfig bettingConfig){
        //领奖阶段
        log("--领奖阶段，领奖时间段：{0} 至 {1}",
                bettingConfig.getPrizeBeginTime(), bettingConfig.getPrizeEndTime());
    }
    //领奖结束阶段
    private void stagePrizingEnd(BettingConfig bettingConfig){
        //领奖结束阶段
        log("--领奖结束阶段，领奖结束时间：{0} " , bettingConfig.getPrizeEndTime());

        //弃奖操作通知
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_AUTO_ABANDON_PRIZE));
    }

    //领红包阶段
    private void stageRedpacket(BettingConfig bettingConfig){
        //抢红包阶段
        log("--抢红包阶段，抢红包时间段：{0} 至 {1}",
                bettingConfig.getRedpacketBeginTime(), bettingConfig.getRedpacketEndTime());
    }

    //领红包结束阶段
    private void stageRedpacketEnd(BettingConfig bettingConfig){
        //抢红包结束阶段
        log("--抢红包结束阶段，抢红包结束时间：{0}" , bettingConfig.getRedpacketEndTime());

        //红包结束通知
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_REDPACKET_END));
    }

    //活动结束
    private void stageFinished(BettingConfig bettingConfig){
        //活动完成 阶段 ，1）更新 活动状态 为“已完成” 2）发送活动完成的任务（奖池沉淀处理任务，包含未领取的奖和未抽取的红包）
        log("--完成阶段，领奖结束时间：{0}，红包领取结束时间：{1}",
                bettingConfig.getPrizeEndTime(), bettingConfig.getRedpacketEndTime());

        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_FINISHED){
            log("--更新状态为‘已完成’---");
            bettingConfigService.asnycUpdateById(bettingConfig.getId(),
                    BettingConfigStatus.STAGE_FINISHED);
        }

        //推送活动结束通知
        this.sendKafkaMessage(bettingStateService.findBettingState(
                bettingConfig.getId(), BettingStateOperateType.OP_NEXT_JACKPOT));
    }

}
