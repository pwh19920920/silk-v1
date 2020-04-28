package com.spark.bitrade.task;

import com.spark.bitrade.constant.BettingStateOperateMark;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.BranchRecordBranchType;
import com.spark.bitrade.constant.BranchRecordBusinessType;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/***
 * 分红任务
 * @author yangch
 * @time 2018.09.14 14:31
 */
@Component
@Slf4j
public class RewardTaskHandlerImpl implements IBettingTaskHandler {

    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private BettingStateService bettingStateService;
    @Autowired
    private BettingRecordService bettingRecordService;
    @Autowired
    private BranchRecordService branchRecordService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Override
    @Async
    public void run(BettingState bettingState) {
        //思路：
        // 检验  bettingState 的状态，未处理则标记为已处理
        // 查询满足分红的用户集合;
        //  1）获取本期投注的所有用户
        //  2）获取本期投注的所有记录及对应的邀请用户ID
        //  3）判断投注记录中的邀请用户ID在‘1）获取本期投注的所有用户’中，有则进行返佣分红，没有则不返佣分红
        // 分红操作
        // 生成分红记录
        // bettingState 的状态更改为 ‘已完成’

        if(bettingState.getMark() == BettingStateOperateMark.UNTREATED) {
            try {
                //更新状态 为‘处理中’
                bettingState.setMark(BettingStateOperateMark.TREATING);
                bettingStateService.save(bettingState);

                BettingConfig bettingConfig = bettingConfigService.findConfigById(bettingState.getPeriodId());

                //获取指定周期的投注用户
                List<String> lstBettingUser = bettingRecordService.queryBettingUserByPeriodId(bettingState.getPeriodId());
                if(null == lstBettingUser || lstBettingUser.size() ==0) {
                    log.warn("没有投注的用户记录");
                } else {
                    //获取指定周期中有推荐邀请关系投注记录
                    List<BettingRecord> lstInviterBettingRecord =
                            bettingRecordService.queryInviterBettingRecordByPeriodId(bettingState.getPeriodId());
                    if(null == lstInviterBettingRecord) {
                        log.warn("本期没有可以参与分红的用户");
                    } else {
                        //投注用户的推荐关系分红处理
                        log.info("本期分红处理的投注记录数量：{}", lstInviterBettingRecord.size());

                        BettingRecord currBettingRecord = null;
                        for (int i = 0, size = lstInviterBettingRecord.size(); i < size ; i++) {
                            currBettingRecord = lstInviterBettingRecord.get(i);
                            if(currBettingRecord.getInviterId() != null
                                    && lstBettingUser.contains(currBettingRecord.getInviterId().toString())) {
                                //满足推荐关系条件，发放推荐分红
                                try {

                                    //判断是否已经发放了分红
                                    if(this.allowReward(bettingState, currBettingRecord)) {
                                        //获取入账的钱包账户
                                        MemberWallet promoteMemberWalletCeche = memberWalletService.findCacheByCoinUnitAndMemberId(
                                                currBettingRecord.getBetSymbol(), currBettingRecord.getInviterId());
                                        if (null == promoteMemberWalletCeche) {
                                            log.warn("该投注记录的返佣分红失败，错误信息：该用户没有对应的钱包账户。被邀请人的投注记录：{}", currBettingRecord);
                                        } else {
                                            //事务处理返佣分红
                                            getService().complete4Transactional(bettingState,
                                                    bettingConfig, currBettingRecord, promoteMemberWalletCeche);
                                        }
                                    } else {
                                        log.info("该投注记录已返佣，投注记录：{}", currBettingRecord);
                                    }
                                }catch (Exception ex){
                                    log.error("该投注记录的返佣分红失败，投注记录:{}，错误信息：{}", currBettingRecord,ex.getMessage());
                                    ex.printStackTrace();
                                }
                            }

                            log.info("本期分红处理进度：{}/{}", i, size);
                        }
                    }
                }

                //更新为 完成
                bettingState.setMark(BettingStateOperateMark.TREATED);
                bettingStateService.save(bettingState);
            }catch (Exception e){
                e.printStackTrace();
                log.error("分红处理失败:{}", e.getMessage());
            }
        } else {
            //正在处理中
            log.info("正在处理中...");
        }
    }

    //分红处理
    @Transactional(rollbackFor = Exception.class)
    public void complete4Transactional(BettingState bettingState, BettingConfig bettingConfig,
                                       BettingRecord bettingRecord, MemberWallet memberWalletCeche){
        //计算分红数目
        BigDecimal rewardAmount = bettingRecord.getBetNum()
                .multiply(bettingConfig.getRebateRatio()).setScale(8, BigDecimal.ROUND_DOWN);

        //保存 返佣记录
        BranchRecord branchRecord = new BranchRecord();
        branchRecord.setPeriodId(bettingState.getPeriodId());
        branchRecord.setSymbol(bettingRecord.getBetSymbol());
        branchRecord.setAmount(rewardAmount);
        branchRecord.setIncomeMemberId(bettingRecord.getInviterId());
        branchRecord.setBranchType(BranchRecordBranchType.INCOME);
        branchRecord.setBusinessType(BranchRecordBusinessType.REWARD);
        branchRecord.setRefId(bettingRecord.getId());
        branchRecord.setSpecial(BooleanEnum.IS_FALSE);

        branchRecordService.save(branchRecord);


        //入账操作
        MessageResult result = memberWalletService.increaseBalance(memberWalletCeche.getId(),rewardAmount);
        if(result.getCode() !=0) {
            new Exception(result.getMessage());
        }
    }

    //检查是否已经做了返佣分红
    private boolean allowReward(BettingState bettingState, BettingRecord bettingRecord){
        List<BranchRecord> list = branchRecordService.queryAllIncomeRecordByCondition(
                bettingState.getPeriodId(), bettingRecord.getInviterId(), BranchRecordBusinessType.REWARD);
        if(null == list) {
            return true;
        }

        long size = list.stream().filter(branchRecord -> bettingRecord.getId() == branchRecord.getRefId()).count();
        if(size > 0){
            return false;
        } else {
            return true;
        }
    }

    public RewardTaskHandlerImpl getService(){
        return SpringContextUtil.getBean(RewardTaskHandlerImpl.class);
    }
}
