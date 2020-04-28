package com.spark.bitrade.task;

import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.GuessActivityController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.processor.CoinExchangeRateService;
import com.spark.bitrade.processor.StatJackpotService;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/***
 * 下期奖池沉淀扣除操作任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class NextJackpotTaskHandlerImpl implements IBettingTaskHandler {

    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private BranchRecordService branchRecordService;
    @Autowired
    private JackpotService jackpotService;
    @Autowired
    private PriceRangeService priceRangeService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private CoinExchangeRateService coinExchangeRateService;
    @Autowired
    private StatJackpotService statJackpotService;
    @Autowired
    private RedisService redisService;

    @Override
    @Async
    public void run(BettingState bettingState) {
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 奖池沉淀
        // 更改 bettingState 的状态 为 ‘已完成’

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //获取开奖价格
                BigDecimal currPrizePrice = bettingConfig.getPrizePrice();
                log.info("info-NextJackpot:获取开奖价格={}", currPrizePrice);

                //计算中奖价格的区间
                Optional<BettingPriceRange> bettingPriceRange =
                        priceRangeService.determinePriceRange(bettingState.getPeriodId(), currPrizePrice);

//                //上一期总奖池数量
//                BigDecimal prevJackpotBalanceTotal = statJackpotService.statPrevJackpot(bettingConfig);
//                log.info("info-NextJackpot:上一期总奖池数量={}", prevJackpotBalanceTotal);
//
//                //本期投注总额，为投注币种
//                BigDecimal total = bettingRecordService.queryBetTotal(bettingState.getPeriodId());
//                log.info("info-NextJackpot:本期投注总额={}", total);

                //本期总奖池数量，为投注币种
                BigDecimal currentJackpotTotal = statJackpotService.statCurrentJackpot(bettingState.getPeriodId());
                log.info("info-NextJackpot:本期总奖池数量={}", currentJackpotTotal);

                //本期领奖总额统计，该币种为奖品币种（一般和竞猜币种相同），需要转换投注币种
                BigDecimal totalOutPrize = rewardService.findRewardAmount(bettingState.getPeriodId(),
                        RewardBusinessType.GUESS, RewardStatus.RECEIVED);
                log.info("info-NextJackpot:1本期领奖总额统计={}", totalOutPrize);
                totalOutPrize = coinExchangeRateService.toRate(totalOutPrize,
                        bettingConfig.getPrizeSymbol(), bettingConfig.getBetSymbol());
                log.info("info-NextJackpot:2本期领奖总额统计={}", totalOutPrize);


                //本期的推荐返佣总额，即10%用于本期的推荐返佣
                BigDecimal rebateRatio = bettingConfig.getRebateRatio(); //返佣比例
                log.info("info-NextJackpot:返佣比例={}", rebateRatio);
                BigDecimal rewardAmountTotal = currentJackpotTotal
                        .multiply(rebateRatio).setScale(8, BigDecimal.ROUND_UP);
                log.info("info-NextJackpot:本期的推荐返佣总额={}", rewardAmountTotal);
                //本期分红总额统计，为投注币种
                BigDecimal rewardAmount =branchRecordService.findBusinessAmount(bettingState.getPeriodId(), BranchRecordBusinessType.REWARD,
                        BranchRecordBranchType.INCOME);
                log.info("info-NextJackpot:本期分红总额统计={}", rewardAmount);
                //本期未返佣完的分红数量
                BigDecimal rewardAmountBlance = rewardAmountTotal.subtract(rewardAmount);
                log.info("info-NextJackpot:本期未返佣完的分红数量={}", rewardAmountBlance);


                //计算本期红包的总数量，转换为投注币种
                BigDecimal redpacketAmountTotal = BigDecimal.ZERO;
                if(bettingConfig.getRedpacketState() == BooleanEnum.IS_TRUE) {
                    BigDecimal redpacketRatio = bettingConfig.getRedpacketRatio(); //红包发布比例
                    log.info("info-NextJackpot:红包发布比例={}", redpacketRatio);
                    redpacketAmountTotal = currentJackpotTotal
                            .multiply(redpacketRatio).setScale(8, BigDecimal.ROUND_UP);
                    log.info("info-NextJackpot:1计算本期红包的数量={}", redpacketAmountTotal);
                    redpacketAmountTotal = coinExchangeRateService.toRate(redpacketAmountTotal,
                            bettingConfig.getRedpacketPrizeSymbol(), bettingConfig.getBetSymbol());
                    log.info("info-NextJackpot:2计算本期红包的数量={}", redpacketAmountTotal);
                } else {
                    log.info("info-statCurr:计算本期红包的数量={}，不发送", redpacketAmountTotal);
                }

                //计算回购的数量，为投注币种
                BigDecimal backRatio = bettingConfig.getBackRatio();    //回购比例
                log.info("info-NextJackpot:回购比例={}", backRatio);
                BigDecimal backAmount = currentJackpotTotal
                        .multiply(backRatio).setScale(8, BigDecimal.ROUND_UP);
                log.info("info-NextJackpot:计算回购的数量={}", backAmount);

                //注，5%已默认留在奖池中了
                //计算当期奖池余量，（上一期奖池余量+ 上一期红包余量 + 本期统计投注总额 - 本期红包总额 -本期回购总额- 本期的推荐返佣总额 - 本期领奖总额）
                BigDecimal jackpotBalance =
                        currentJackpotTotal
                                .subtract(redpacketAmountTotal).subtract(backAmount).subtract(rewardAmountTotal).subtract(totalOutPrize);
                log.info("info-NextJackpot:计算当期奖池余量={}", jackpotBalance);

                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig ,currPrizePrice, bettingPriceRange, jackpotBalance, rewardAmountBlance);

                //奖池沉淀完后清理奖池的缓存数据
                redisService.remove(RedPacketConstant.JACKPOT_BALANCE + "_" + bettingConfig.getGuessSymbol().toUpperCase());
            }catch (Exception e){
                e.printStackTrace();
                log.error("下期奖池沉淀操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //开奖及中奖用户记录处理
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig,
                                       BigDecimal currPrizePrice, Optional<BettingPriceRange> bettingPriceRange,
                                       BigDecimal jackpotBalance, BigDecimal rewardAmountBlance){
        //生成奖池记录
        Jackpot jackpot = jackpotService.findByPeriodId(bettingState.getPeriodId());
        if(null == jackpot) {
            jackpot = new Jackpot();

            jackpot.setPeriodId(bettingState.getPeriodId());
            jackpot.setPrizePrice(currPrizePrice);
            if(bettingPriceRange.isPresent()) {
                jackpot.setRangeId(bettingPriceRange.get().getId()); //设置中奖的价格区间
            }
            jackpot.setJackpotBalance(jackpotBalance);    //当期奖池余量
            if(jackpot.getRedpacketBalance() == null) {
                jackpot.setRedpacketBalance(BigDecimal.ZERO);  //当期红包余量，由红包结束后再更改此值
            }
            jackpot.setPrizeSymbol(bettingConfig.getPrizeSymbol());
            jackpot.setRedpacketSymbol(bettingConfig.getRedpacketPrizeSymbol());

            jackpotService.save(jackpot);
            log.info("info-NextJackpot:当期红包余量={}", jackpot.getRedpacketBalance());
        } else {
            jackpotService.updateJackpotJalance(bettingState.getPeriodId(), jackpot.getId(), jackpotBalance);
            log.info("info-NextJackpot:更新奖池余额");
        }

        //每期推荐分红扣出后剩余的部分 放到回购中
        //生成回购记录
        BranchRecord branchRecord = new BranchRecord();
        branchRecord.setPeriodId(bettingState.getPeriodId());
        branchRecord.setSymbol(bettingConfig.getBetSymbol());
        branchRecord.setAmount(rewardAmountBlance);
        branchRecord.setBranchType(BranchRecordBranchType.INCOME);
        branchRecord.setBusinessType(BranchRecordBusinessType.REWARD_BALANCE);  //返佣余额
        branchRecord.setSpecial(BooleanEnum.IS_TRUE);

        branchRecordService.save(branchRecord);


        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public NextJackpotTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(NextJackpotTaskHandlerImpl.class);
    }
}
