package com.spark.bitrade.task;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;

import com.spark.bitrade.processor.RedPacketSettlementService;

import com.spark.bitrade.service.BettingConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


/**
 * <p>红包完成处理器</p>
 * @author octopus
 */
@Component
@Slf4j
public class RedPacketFinishTaskHandlerImpl implements IBettingTaskHandler {

    @Autowired
    private RedPacketSettlementService redPacketSettlementService;

    @Autowired
    private BettingConfigService bettingConfigService;

    @Override
//    @Async
    public void run(BettingState bettingState) {
        //红包领取结束业务处理，处理完之后更新为完成状态
        if (BettingStateOperateType.OP_REDPACKET_END == bettingState.getOperate() &&
                BettingStateOperateMark.UNTREATED == bettingState.getMark()) {

            BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());
            if(null == bettingConfig){
                log.info("**********当前活动不存在**********");
                return;
            }

            //判断活动是否被删除
            if(bettingConfig.getDeleted() == BooleanEnum.IS_TRUE){
                log.info("**********当前活动已被删除**********");
                return;
            }
            //红包功能未开启
            if(bettingConfig.getRedpacketState() == BooleanEnum.IS_FALSE){
                log.info("**********当前活动未开启红包功能**********");
                return;
            }
            //活动是否已经结束
            if(bettingConfig.getStatus() == BettingConfigStatus.STAGE_FINISHED){
                log.info("**********当前活动已完成**********");
                return;
            }
            log.info("*************红包余额更新处理开始****************");
            redPacketSettlementService.redPacketSettlement(bettingState);
            log.info("*************红包余额更新处理结束****************");
        }
    }
}
