package com.spark.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.constant.GuessKafkaTopicConstant;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.service.BettingStateService;
import com.spark.bitrade.task.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/***
 * 消费竞猜活动的kafka消息
 * @author yangch
 * @time 2018.09.14 14:33
 */

@Component
@Slf4j
public class PromoteGuestConsumer {
    @Autowired
    private RewardTaskHandlerImpl rewardTaskHandler;
    @Autowired
    private MarkGuessPriceTaskHandlerImpl markGuessPriceTaskHandler;
    @Autowired
    private PraiseTaskHandlerImpl praiseTaskHandler;
    @Autowired
    private PraiseNotificationTaskHandlerImpl praiseNotificationTaskHandler;
    @Autowired
    private BuyBackTaskHandlerImpl buyBackTaskHandler;
    @Autowired
    private TakeOutRedPacketTaskHandlerImpl takeOutRedPacketTaskHandler;
    @Autowired
    private AutoAbandonPrizeTaskHandlerImpl autoAbandonPrizeTaskHandler;
    @Autowired
    private NextJackpotTaskHandlerImpl nextJackpotTaskHandler;
    @Autowired
    private RedPacketFinishTaskHandlerImpl redPacketFinishTaskHandler;
    @Autowired
    private RedPacketReadyTaskHandlerImpl redPacketReadyTaskHandler;

    @Autowired
    private BettingStateService bettingStateService;

    @KafkaListener(topics = GuessKafkaTopicConstant.TX_PROMOTE_GUESS_HANDLER ,group = "group-handle")
    public void handle(ConsumerRecord<String,String> record){
        BettingStateOperateType key = BettingStateOperateType.valueOfOrdinal(Integer.valueOf(record.key()));
        BettingState bettingState = JSON.parseObject(record.value(), BettingState.class);
        //防止重做，当状态为 未处理 的时候 重新从数据库中获取数据
        if(bettingState.getMark() != BettingStateOperateMark.TREATED) {
            bettingState = bettingStateService.findBettingState(bettingState.getPeriodId(), bettingState.getOperate());
        }

        switch (key){
            case OP_REWARD: //分红返佣操作通知
                rewardTaskHandler.run(bettingState);
                break;
            case OP_MARK_GUESS_PRICE: //开奖通知
                markGuessPriceTaskHandler.run(bettingState);
                break;
            case OP_PRAISE : //中奖奖励操作
                praiseTaskHandler.run(bettingState);
                break;
            case OP_PRAISE_NOTIFICATION : //中奖通知
                praiseNotificationTaskHandler.run(bettingState);
                break;
            case OP_BUY_BACK : //回购操作
                buyBackTaskHandler.run(bettingState);
                break;
            case OP_REDPACKET : //奖池红包扣除操作通知
                takeOutRedPacketTaskHandler.run(bettingState);
                break;
            case OP_AUTO_ABANDON_PRIZE : //弃奖操作通知
                autoAbandonPrizeTaskHandler.run(bettingState);
                break;
            case OP_REDPACKET_END : //红包结束通知
                redPacketFinishTaskHandler.run(bettingState);
                break;
            case OP_NEXT_JACKPOT : //下期奖池沉淀扣除操作
                nextJackpotTaskHandler.run(bettingState);
                break;
            case OP_REDPACKET_READY : //红包准备操作
                redPacketReadyTaskHandler.run(bettingState);
                break;
        }
    }
}
