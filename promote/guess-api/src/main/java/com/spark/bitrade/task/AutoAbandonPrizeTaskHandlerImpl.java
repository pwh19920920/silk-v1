package com.spark.bitrade.task;

import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.service.BettingStateService;
import com.spark.bitrade.service.RewardService;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/***
 * 弃奖操作任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class AutoAbandonPrizeTaskHandlerImpl implements IBettingTaskHandler {
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private RewardService rewardService;


    @Override
    @Async
    public void run(BettingState bettingState) {
        System.out.println("---------AutoAbandonPrizeTaskHandlerImpl-----------");

        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 弃奖用户的记录处理
        // 标记 bettingState 的状态 为‘已完成’

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                //事务处理
                getService().complete4Transactional(bettingState);

                //消息通知 处理
            }catch (Exception e){
                e.printStackTrace();
                log.error("弃奖操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //处理弃奖用户记录
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState){
        //弃奖处理
        rewardService.autoAbandonPrize(bettingState.getPeriodId());

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public AutoAbandonPrizeTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(AutoAbandonPrizeTaskHandlerImpl.class);
    }
}
