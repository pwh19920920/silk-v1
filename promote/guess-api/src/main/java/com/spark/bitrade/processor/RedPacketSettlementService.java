package com.spark.bitrade.processor;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>红包结算服务</p>
 * @author octopus
 */
@Service
@Slf4j
public class RedPacketSettlementService {

    @Autowired
    private BettingStateService bettingStateService;

    @Autowired
    private JackpotService jackpotService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private BranchRecordService branchRecordService;


    /**
     * 红包余额结算,需要更新状态和红包余额
     * @param bettingState
     *             分配标记
     */
    @Transactional(rollbackFor = Exception.class)
    public void redPacketSettlement(BettingState bettingState){

        //查询本期红包中奖总额
        Map<String,Object> params = new HashMap<>();
        params.put("id",bettingState.getPeriodId());
        params.put("businessType", RewardBusinessType.REDPACKET.getCode());
        params.put("status", RewardStatus.PRIZE.getCode());
        BigDecimal grantAmount = rewardService.findRewardAmount(params);

        //查询红包扣除
        //根据投注期数查询当期红包扣除数量
        Map<String,Object> currentParams = new HashMap<>();
        currentParams.put("id",bettingState.getPeriodId());
        currentParams.put("businessType", BranchRecordBusinessType.RED_PACKET_TOTAL.getCode());
        BigDecimal redpacketNum = branchRecordService.findRedpacketDeductAmount(currentParams);

        //余额初始化为0
        BigDecimal balance = BigDecimal.ZERO;


        if(null != redpacketNum){
            balance = balance.add(redpacketNum);
        }

        //减去中奖红包总额
        if(null != grantAmount){
            balance = balance.subtract(grantAmount).setScale(RedPacketConstant.DIGITS,BigDecimal.ROUND_DOWN);
        }

        log.info("*************本期红包中奖总额：{} *************",grantAmount);
        log.info("*************本期红包扣除总额：{} *************",redpacketNum);
        log.info("*************本期红包剩余总额：{} *************",balance);
        //修改状态为已完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        Jackpot jackpot = jackpotService.findByPeriodId(bettingState.getPeriodId());
        //修改本期奖池红包余额
        jackpot.setRedpacketBalance(balance);
        bettingState = bettingStateService.save(bettingState);
        //换回jpa的api
        //int count = jackpotService.updateRedpacketBalance(jackpot);
        jackpot = jackpotService.save(jackpot);
        log.info("红包完成状态result={}",bettingState);
        log.info("奖池result={}",jackpot);
        log.info("*************奖池红包余额更新完成*************");

    }






}
