package com.spark.bitrade.task;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.constant.GuessKafkaTopicConstant;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.GuessCoin;
import com.spark.bitrade.service.BettingConfigService;
import com.spark.bitrade.service.BettingStateService;
import com.spark.bitrade.service.GuessCoinService;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/***
 * 开奖操作任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class MarkGuessPriceTaskHandlerImpl implements IBettingTaskHandler {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;

    @Autowired
    private GuessCoinService guessCoinService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    @Async
    public void run(BettingState bettingState) {
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 查询当前的全网价格
        // 记录中奖价格
        // 标记 bettingState 的状态为‘已完成’
        // 通知 ‘中奖奖励操作’
        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //获取开奖价格
                //GuessCoin guessCoin = this.getBTC();
                GuessCoin guessCoin = this.getGuessCoin(bettingConfig.getGuessSymbol());
                if(null == guessCoin || null == guessCoin.getPriceUsd()) {
                    log.error("未能获取到价格");
                    return;
                }
                BigDecimal currPrizePrice = new BigDecimal(guessCoin.getPriceUsd());

                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig, currPrizePrice);

                //发送kafka消息，提前通知 ‘中奖奖励操作’
                this.sendKafkaMessage(bettingStateService.findBettingState(
                        bettingConfig.getId(), BettingStateOperateType.OP_PRAISE));
            }catch (Exception e){
                e.printStackTrace();
                log.error("开奖及中奖处理操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //开奖记录处理
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig,
                                       BigDecimal currPrizePrice){
        //更新配置中的中奖价格
        bettingConfigService.updatePrizePriceById(bettingConfig.getId(), currPrizePrice);

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public MarkGuessPriceTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(MarkGuessPriceTaskHandlerImpl.class);
    }

    /**
     * 币种名称
     * @param coinUintName
     * @return
     */
    public GuessCoin getGuessCoin(String coinUintName){
        if("BTC".equalsIgnoreCase(coinUintName)) {
            return guessCoinService.getGuessCoinRealtime("bitcoin");
        } else if("BTMC".equalsIgnoreCase(coinUintName)) {
            return guessCoinService.getGuessCoinRealtime("BTMC");
        }

        return null;
    }

    //获取btc的全网价格数据
    private GuessCoin getBTC(){
        return guessCoinService.getGuessCoinRealtime("bitcoin");
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
