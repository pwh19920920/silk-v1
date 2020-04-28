package com.spark.bitrade.task;

import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingPriceRange;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/***
 * 中奖通知任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class PraiseNotificationTaskHandlerImpl implements IBettingTaskHandler {
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private PriceRangeService priceRangeService;
    @Autowired
    private RewardService rewardService;
    @Autowired
    private ActivitySmsSendService activitySmsSendService;

    @Override
    @Async
    public void run(BettingState bettingState) {
        System.out.println("---------PraiseTaskHandlerImpl-----------");

        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 获取中奖的用户
        // 短信通知用户
        // 标记 bettingState 的状态为‘已完成’

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //获取开奖价格
                BigDecimal currPrizePrice = bettingConfig.getPrizePrice();

                //计算中奖价格的区间
                Optional<BettingPriceRange> bettingPriceRange =
                        priceRangeService.determinePriceRange(bettingState.getPeriodId(), currPrizePrice);
                //获取需要短信通知的用户  todo 获取中奖用户
                activitySmsSendService.dealSendSms(bettingPriceRange);
                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig );
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
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig){

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public PraiseNotificationTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(PraiseNotificationTaskHandlerImpl.class);
    }

}
