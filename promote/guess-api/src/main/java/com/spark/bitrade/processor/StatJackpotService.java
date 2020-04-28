package com.spark.bitrade.processor;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/***
 * 
 * @author yangch
 * @time 2018.09.19 20:43
 */

@Service
@Slf4j
public class StatJackpotService {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;

    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private JackpotService jackpotService;
    @Autowired
    private BranchRecordService branchRecordService;

    /**
     * 上一期的奖池数量
     * @param bettingConfig 本期的活动配置
     * @return
     */
    public BigDecimal statPrevJackpot(BettingConfig bettingConfig){
        //上一期奖池
        Jackpot prevJackpot =  jackpotService.findLastByPeriodId(bettingConfig.getId());

        BigDecimal prevJackpotBalance = prevJackpot.getJackpotBalance();    //上一期奖池余量，为投注币种
        BigDecimal prevRedpacketBalance = prevJackpot.getRedpacketBalance();  //上一期红包余量， 注意 可能需要转化为投注币种
        if(prevJackpot.getRedpacketBalance().compareTo(BigDecimal.ZERO)>0){
            //转换为投注币种
            prevRedpacketBalance = coinExchangeRateService.toRate(prevJackpot.getRedpacketBalance(),
                    bettingConfig.getRedpacketPrizeSymbol(), bettingConfig.getBetSymbol());
        }
        //上一期总奖池数量 = 上一期奖池余量 + 上一期红包余量
        return prevJackpotBalance.add(prevRedpacketBalance);
    }

    //查询奖池
    public BigDecimal statJackpot(long periodId){
        log.info("info-statJackpot:投注ID={}", periodId);
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
        if(null == bettingConfig){
            return BigDecimal.ZERO;
        }
        Jackpot jackpot = jackpotService.findByPeriodId(bettingConfig.getId());
        if(null == jackpot) {
            return BigDecimal.ZERO;
        }

        BigDecimal jackpotBalance = jackpot.getJackpotBalance();    // 奖池余量，为投注币种
        log.info("info-statJackpot:奖池余量={}", jackpotBalance);
        BigDecimal redpacketBalance = jackpot.getRedpacketBalance();  // 红包余量， 注意 可能需要转化为投注币种
        log.info("info-statJackpot:1红包余量={}", redpacketBalance);
        if(redpacketBalance.compareTo(BigDecimal.ZERO)>0){
            //转换为投注币种
            redpacketBalance = coinExchangeRateService.toRate(redpacketBalance,
                    bettingConfig.getRedpacketPrizeSymbol(), bettingConfig.getBetSymbol());
        }
        log.info("info-statJackpot:2红包余量={}", redpacketBalance);
        log.info("info-statJackpot:总奖池数量={}", jackpotBalance.add(redpacketBalance));
        //总奖池数量 = 奖池余量 + 红包余量
        return jackpotBalance.add(redpacketBalance);
    }

    /**
     * 获取当前奖池数量
     *
     * @param periodId 本期ID
     * @return 奖池数量
     */
    public BigDecimal statCurrentJackpot(long periodId){
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);

        //上一期总奖池数量 = 上一期奖池余量 + 上一期红包余量
        BigDecimal prevJackpotBalanceTotal = statPrevJackpot(bettingConfig);
        log.info("info-statCurr:上一期总奖池数量={}", prevJackpotBalanceTotal);

        //本期的投注总数量
        BigDecimal total = bettingRecordService.queryBetTotal(periodId);
        log.info("info-statCurr:本期的投注总数量={}", total);

        return prevJackpotBalanceTotal.add(total);
    }

    /**
     * 获取当前奖池数量（废弃）
     *
     * @param periodId 本期ID
     * @return 奖池数量
     */
    public BigDecimal statCurrentJackpot_bak(long periodId){

        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);
//        //上一期奖池
//        Jackpot prevJackpot =  jackpotService.findLastByPeriodId(periodId);
//
//        BigDecimal prevJackpotBalance = prevJackpot.getJackpotBalance();    //上一期奖池余量，为投注币种
//        BigDecimal prevRedpacketBalance = prevJackpot.getRedpacketBalance();  //上一期红包余量， 注意 可能需要转化为投注币种
//        if(prevJackpot.getRedpacketBalance().compareTo(BigDecimal.ZERO)>0){
//            //转换为投注币种
//            prevRedpacketBalance = coinExchangeRateService.toRate(prevJackpot.getRedpacketBalance(),
//                    bettingConfig.getRedpacketSymbol(), bettingConfig.getBetSymbol());
//        }
//        //上一期总奖池数量 = 上一期奖池余量 + 上一期红包余量
//        BigDecimal prevJackpotBalanceTotal = prevJackpotBalance.add(prevRedpacketBalance);
        BigDecimal prevJackpotBalanceTotal = statPrevJackpot(bettingConfig);
        log.info("info-statCurr:上一期总奖池数量={}", prevJackpotBalanceTotal);


        //本期的投注总数量
        BigDecimal total = bettingRecordService.queryBetTotal(periodId);
        log.info("info-statCurr:本期的投注总数量={}", total);

        //本期的回购数量
        BigDecimal backRatio = bettingConfig.getBackRatio();    //回购比例
        BigDecimal backAmount = total.add(prevJackpotBalanceTotal).
                multiply(backRatio).setScale(8, BigDecimal.ROUND_UP); //注意 加上一期的沉淀奖池
        log.info("info-statCurr:本期的回购数量={},回购比例={}", backAmount, backRatio);

        //本期分红总额统计，为投注币种
        BigDecimal rewardAmount = branchRecordService.findBusinessAmount(periodId, BranchRecordBusinessType.REWARD,
                BranchRecordBranchType.INCOME);
        log.info("info-statCurr:本期分红总额统计={}", rewardAmount);

        //计算本期红包的数量
        BigDecimal redpacketAmount = BigDecimal.ZERO;
        if(bettingConfig.getRedpacketState() == BooleanEnum.IS_TRUE) {
            BigDecimal redpacketRatio = bettingConfig.getRedpacketRatio(); //红包发布比例
            redpacketAmount = total.add(prevJackpotBalanceTotal)
                    .multiply(redpacketRatio).setScale(8, BigDecimal.ROUND_UP); //注意 加上一期的沉淀奖池
            log.info("info-statCurr:计算本期红包的数量={}, 红包发布比例={}", redpacketAmount, redpacketRatio);
            redpacketAmount = coinExchangeRateService.toRate(redpacketAmount,
                    bettingConfig.getRedpacketPrizeSymbol(), bettingConfig.getBetSymbol());
            log.info("info-NextJackpot:2计算本期红包的数量={}", redpacketAmount);
        } else {
            log.info("info-statCurr:计算本期红包的数量={}，不发送", redpacketAmount);
        }

        //本期奖池沉淀数量
        BigDecimal nextPeriodRatio = bettingConfig.getNextPeriodRatio(); //下期奖池沉淀比例
        BigDecimal nextPeriodAmount = total.add(prevJackpotBalanceTotal)
                .multiply(nextPeriodRatio).setScale(8, BigDecimal.ROUND_UP); //注意 加上一期的沉淀奖池
        log.info("info-statCurr:本期奖池沉淀数量={}, 下期奖池沉淀比例={}", nextPeriodAmount, nextPeriodRatio);

        //本期奖池 = 本期统计投注总额 - 本期回购总额 - 本期分红总额  - 本期红包总额 - 本期沉淀总额
        BigDecimal currJackpotBalanceTotal =
                total.subtract(backAmount).subtract(rewardAmount).subtract(redpacketAmount).subtract(nextPeriodAmount);
        log.info("info-statCurr:本期奖池={}", currJackpotBalanceTotal);

        //计算当期奖池余量 = （上一期总奖池数量 + 本期奖池）
        return prevJackpotBalanceTotal.add(currJackpotBalanceTotal);
    }
}
