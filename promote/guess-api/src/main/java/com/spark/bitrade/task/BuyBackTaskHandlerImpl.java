package com.spark.bitrade.task;

import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.BettingState;
import com.spark.bitrade.entity.BranchRecord;
import com.spark.bitrade.processor.StatJackpotService;
import com.spark.bitrade.service.BettingConfigService;
import com.spark.bitrade.service.BettingRecordService;
import com.spark.bitrade.service.BettingStateService;
import com.spark.bitrade.service.BranchRecordService;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/***
 * 回购操作任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class BuyBackTaskHandlerImpl implements IBettingTaskHandler {
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

    @Override
    @Async
    public void run(BettingState bettingState) {
        //System.out.println("---------BuyBackTaskHandlerImpl-----------");
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 查询所有下注记录的数量和
        // 按回购比例扣除
        // 生成回购记录

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //上一期总奖池数量
                BigDecimal prevJackpotBalanceTotal = statJackpotService.statPrevJackpot(bettingConfig);
                log.info("info-NextJackpot:上一期总奖池数量={}", prevJackpotBalanceTotal);

                BigDecimal total = bettingRecordService.queryBetTotal(bettingState.getPeriodId());
                BigDecimal backRatio = bettingConfig.getBackRatio();

                //计算回购的数量
                BigDecimal backAmount = total.add(prevJackpotBalanceTotal)
                        .multiply(backRatio).setScale(8, BigDecimal.ROUND_UP);

                //事务处理
                getService().complete4Transactional(bettingState, bettingConfig, backAmount);
            }catch (Exception e){
                e.printStackTrace();
                log.error("回购操作失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //回购逻辑
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig, BigDecimal backAmount){
        //生成回购记录
        BranchRecord branchRecord = new BranchRecord();
        branchRecord.setPeriodId(bettingState.getPeriodId());
        branchRecord.setSymbol(bettingConfig.getBetSymbol());
        branchRecord.setAmount(backAmount);
        branchRecord.setBranchType(BranchRecordBranchType.INCOME);
        branchRecord.setBusinessType(BranchRecordBusinessType.BUY_BACK);
        branchRecord.setSpecial(BooleanEnum.IS_TRUE);

        branchRecordService.save(branchRecord);

        //更新为 完成
        bettingState.setMark(BettingStateOperateMark.TREATED);
        bettingStateService.save(bettingState);
    }

    public BuyBackTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(BuyBackTaskHandlerImpl.class);
    }
}
