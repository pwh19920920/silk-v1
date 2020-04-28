package com.spark.bitrade.task;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.BranchRecord;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.processor.CoinExchangeRateService;
import com.spark.bitrade.processor.StatJackpotService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/***
 * 红包扣除操作任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class TakeOutRedPacketTaskHandlerImpl implements IBettingTaskHandler {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private BranchRecordService branchRecordService;
    @Autowired
    private StatJackpotService statJackpotService;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    @Async
    public void run(BettingState bettingState) {
        //System.out.println("---------TakeOutRedPacketTaskHandlerImpl-----------");
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 查询所有下注记录的数量和
        // 判断 是否要开启红包
        // 按红包比例扣除，并生成本期红包发放总数的记录
        // 推送“红包准备”消息

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //上一期总奖池数量
                BigDecimal prevJackpotBalanceTotal = statJackpotService.statPrevJackpot(bettingConfig);
                log.info("info-takeOut:上一期总奖池数量={}", prevJackpotBalanceTotal);

                //本期的投注总数量
                BigDecimal total = bettingRecordService.queryBetTotal(bettingState.getPeriodId());
                BigDecimal redpacketRatio = bettingConfig.getRedpacketRatio(); //红包发布比例
                log.info("info-takeOut:本期的投注总数量={}", total);
                log.info("info-takeOut:红包发布比例={}", redpacketRatio);

                //判断是否开启红包,红包活动开启条件：奖池≥50EOS
                BigDecimal redpacketOpenLimit =
                        bettingConfig.getRedpacketOpenLimit()==null ? BigDecimal.ZERO : bettingConfig.getRedpacketOpenLimit();
                boolean openRedpacket = false;
                if(total.add(prevJackpotBalanceTotal).compareTo(redpacketOpenLimit) >= 0) {
                    openRedpacket = true;
                }

                BigDecimal redpacketAmount = BigDecimal.ZERO;
                if(openRedpacket) {
                    //计算本期红包的总金额
                    redpacketAmount = total.add(prevJackpotBalanceTotal)
                            .multiply(redpacketRatio).setScale(8, BigDecimal.ROUND_UP); //注意 已加上一期的沉淀奖池
                }

                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig, openRedpacket, redpacketAmount);

                //推送‘红包准备’任务消息（等‘红包扣除’完成后再准备红包）
                this.sendKafkaMessage(bettingStateService.findBettingState(
                        bettingConfig.getId(), BettingStateOperateType.OP_REDPACKET_READY));
            }catch (Exception e){
                e.printStackTrace();
                log.error("从奖池中扣减红包操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //本期红包扣除逻辑
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig,
                                       boolean openRedpacket, BigDecimal redpacketAmount){
        //生成红包扣减记录
        BranchRecord branchRecord = new BranchRecord();
        branchRecord.setPeriodId(bettingState.getPeriodId());
        branchRecord.setSymbol(bettingConfig.getBetSymbol());
        branchRecord.setAmount(redpacketAmount);
        branchRecord.setBranchType(BranchRecordBranchType.INCOME);
        branchRecord.setBusinessType(BranchRecordBusinessType.RED_PACKET_TOTAL);
        branchRecord.setSpecial(BooleanEnum.IS_TRUE);

        branchRecordService.save(branchRecord);

        //更新红包为 开启 状态
        if( openRedpacket
                && bettingConfig.getRedpacketState() == BooleanEnum.IS_FALSE){
            //开启红包
            bettingConfigService.updateConfigRedpacketStateById(bettingState.getPeriodId(), BooleanEnum.IS_TRUE);
        } else if(openRedpacket == false
                && bettingConfig.getRedpacketState() == BooleanEnum.IS_TRUE) {
            //关闭 红包
            bettingConfigService.updateConfigRedpacketStateById(bettingState.getPeriodId(), BooleanEnum.IS_FALSE);
        }

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public TakeOutRedPacketTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(TakeOutRedPacketTaskHandlerImpl.class);
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
}
