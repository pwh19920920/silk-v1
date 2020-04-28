package com.spark.bitrade.task;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BettingStateOperateType;
import com.spark.bitrade.constant.GuessKafkaTopicConstant;
import com.spark.bitrade.dao.BettingConfigDao;
import com.spark.bitrade.entity.*;
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
import java.util.Optional;

/***
  * 中奖奖励操作任务
  * @author yangch
  * @time 2018.09.14 14:31
  */
@Component
@Slf4j
public class PraiseTaskHandlerImpl implements IBettingTaskHandler {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private PriceRangeService priceRangeService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;
    @Autowired
    private StatJackpotService statJackpotService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private JackpotService jackpotService;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    BettingConfigDao bettingConfigDao;

    @Override
    @Async
    public void run(BettingState bettingState) {
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 记录中奖价格
        // 生成中奖记录
        // 设置 本周期的竞猜状态为 ‘领奖状态’
        // 标记 bettingState 的状态为‘已完成’
        // 通知 ‘中奖通知’
        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                log.info("info-praise:竞猜配置的ID={}", bettingState.getPeriodId());
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigDao.findOne(bettingState.getPeriodId());
                log.info("info-praise:实时获取竞猜配置中的价格={}", bettingConfig.getPrizePrice());
                //BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());
                if(bettingConfig.getPrizePrice() == null) {
                    //获取开奖价格为null的情况，实时的从数据库中获取
                    bettingConfig = bettingConfigService.findConfigByIdRealtime(bettingState.getPeriodId());
                }

                //获取开奖价格
                BigDecimal currPrizePrice = bettingConfig.getPrizePrice();
                log.info("info-praise:获取开奖价格={}", currPrizePrice);

                //计算中奖价格的区间
                Optional<BettingPriceRange> bettingPriceRange =
                        priceRangeService.determinePriceRange(bettingState.getPeriodId(), currPrizePrice);


                //中奖用户的兑换汇率计算
                BigDecimal cashPrizeRatio = BigDecimal.ZERO;
                if(bettingPriceRange.isPresent()) {
                    log.info("info-praise:中奖价格的区间={}", bettingPriceRange.get().getId());

                    //投注币种与奖品的汇率
                    BigDecimal toPrizeRate = coinExchangeRateService.toRate(bettingConfig.getBetSymbol(), bettingConfig.getPrizeSymbol());
                    log.info("info-praise:投注币种与奖品的汇率={},投注币种={},中奖奖励币种={}",
                            toPrizeRate, bettingConfig.getBetSymbol(), bettingConfig.getPrizeSymbol());

                    //本期总奖池数量，为投注币种
                    BigDecimal currentJackpotTotal = statJackpotService.statCurrentJackpot(bettingState.getPeriodId());
                    log.info("info-praise:本期总奖池数量={}", currentJackpotTotal);

                    //本期中奖奖池数量=本期总奖池数量*65%(本期中奖奖池比例)，为投注币种
                    //本期中奖奖池比例
                    BigDecimal prizeRatio = bettingConfig.getPrizeRatio();
                    log.info("info-praise:本期中奖奖池比例={}", prizeRatio);
                    BigDecimal currentJackpot = currentJackpotTotal.multiply(prizeRatio).setScale(8, BigDecimal.ROUND_DOWN);
                    log.info("info-praise:本期中奖奖池数量={}", currentJackpot);

                    //中奖用户的总投注数量，为投注币种
                    BigDecimal currBetTotalByPriceRange = bettingRecordService.queryBetTotalByPriceRange(bettingState.getPeriodId(),
                            bettingPriceRange.get().getId());
                    log.info("info-praise:中奖用户的总投注数量={}", currBetTotalByPriceRange);

                    //中奖用户的兑换汇率
                    BigDecimal rate = BigDecimal.ZERO;
                    if(currBetTotalByPriceRange.compareTo(BigDecimal.ZERO)>0) {
                        rate = currentJackpot.divide(currBetTotalByPriceRange, 8, BigDecimal.ROUND_DOWN);
                    }
                    log.info("info-praise:中奖用户的兑换汇率={}", rate);

                    //按投注币种计算中奖兑换汇率
                    cashPrizeRatio = rate.multiply(toPrizeRate).setScale(8, BigDecimal.ROUND_DOWN);
                    log.info("info-praise:按投注币种计算中奖兑换汇率={}", cashPrizeRatio);
                }

                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig, bettingPriceRange, cashPrizeRatio);

                //中奖消息推送 通知
                this.sendKafkaMessage(bettingStateService.findBettingState(
                        bettingConfig.getId(), BettingStateOperateType.OP_PRAISE_NOTIFICATION));
            }catch (Exception e){
                e.printStackTrace();
                log.error("中奖奖励操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //开奖及中奖用户记录处理
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig,
                                       Optional<BettingPriceRange> bettingPriceRangeOptional, BigDecimal cashPrizeRatio ){
        //创建用户的中奖记录
        if(bettingPriceRangeOptional.isPresent()) {
            BettingPriceRange bettingPriceRange = bettingPriceRangeOptional.get();
            String cashSymbol = bettingConfig.getPrizeSymbol();
            rewardService.batchSavePrizeRecord(bettingState.getPeriodId(),
                    bettingPriceRange.getId(), cashPrizeRatio, cashSymbol);
            bettingRecordService.updatePraiseStatus(bettingPriceRange.getId());
            bettingRecordService.updateLostStatus(bettingState.getPeriodId(), bettingPriceRange.getId());
        } else {
            log.info("本期没有中奖用户。。。");
            bettingRecordService.updateLostStatus(bettingState.getPeriodId(), -1);
        }


        //生成奖池记录
        Jackpot jackpot = jackpotService.findByPeriodId(bettingState.getPeriodId());
        if(null == jackpot) {
            jackpot = new Jackpot();
        }
        jackpot.setPeriodId(bettingState.getPeriodId());
        jackpot.setPrizePrice(bettingConfig.getPrizePrice()==null? BigDecimal.ZERO : bettingConfig.getPrizePrice());
        if(bettingPriceRangeOptional.isPresent()) {
            jackpot.setRangeId(bettingPriceRangeOptional.get().getId()); //设置中奖的价格区间
        }
        jackpot.setJackpotBalance(BigDecimal.ZERO);    //当期奖池余量，奖池沉淀时再更新改值
        jackpot.setRedpacketBalance(BigDecimal.ZERO);  //当期红包余量，奖池沉淀时再更新改值
        jackpot.setPrizeSymbol(bettingConfig.getPrizeSymbol());
        jackpot.setRedpacketSymbol(bettingConfig.getRedpacketPrizeSymbol());
        jackpotService.save(jackpot);

        //更改为 ‘领奖’状态
        if(bettingConfig.getStatus() != BettingConfigStatus.STAGE_PRIZING){
            log.debug("更新状态为‘领奖中’");
            bettingConfigService.updateById(bettingConfig.getId(),
                    BettingConfigStatus.STAGE_PRIZING);
        }

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
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

    public PraiseTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(PraiseTaskHandlerImpl.class);
    }

}
