package com.spark.bitrade.jobhandler;

import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.constant.LockBackStatus;
import com.spark.bitrade.constant.LockRewardType;
import com.spark.bitrade.entity.LockMarketExtraRewardConfig;
import com.spark.bitrade.entity.LockMarketRewardDetail;
import com.spark.bitrade.entity.LockMarketRewardIncomePlan;
import com.spark.bitrade.service.LockMarketExtraRewardConfigService;
import com.spark.bitrade.service.LockMarketRewardDetailService;
import com.spark.bitrade.service.LockMarketRewardIncomePlanService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@JobHandler(value = "cnytLockExtraRewardJobHandler")
@Component
public class CNYTLockExtraRewardJobHandler extends IJobHandler{

    @Autowired
    private LockMarketExtraRewardConfigService lockMarketExtraRewardConfigService;

    @Autowired
    private LockMarketRewardDetailService lockMarketRewardDetailService;

    @Autowired
    private LockMarketRewardIncomePlanService lockMarketRewardIncomePlanService;

    @Override
    @MybatisTransactional(rollbackFor = Exception.class)
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("开始执行CNYT额外奖励统计");
        try{
            XxlJobLogger.log("开始查找额外奖励有效配置");
            List<LockMarketExtraRewardConfig> extraRewardConfigs = lockMarketExtraRewardConfigService.getActivityLists();
            XxlJobLogger.log("查找额外奖励有效配置成功");

            // 开始时间 上一个月最后一天16:00 后
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            //将小时至0
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            //将分钟至0
            calendar.set(Calendar.MINUTE, 0);
            //将秒至0
            calendar.set(Calendar.SECOND,0);
            calendar.add(Calendar.HOUR, -8);
            Date firstDayOfMounth = calendar.getTime();

            //  结束时间 当月最后一天16:00 前
            calendar.add(Calendar.HOUR, +8);
            //将当前月加1；
            calendar.add(Calendar.MONTH, 1);
            //在当前月的下一月基础上减去8个小时
            calendar.add(Calendar.HOUR, -8);
            Date lastDayOfMounth = calendar.getTime();

            XxlJobLogger.log("开始计算有效配置会员的额外奖励");
            // 设置返还奖励时间时间为下月月初8点
            calendar.add(calendar.HOUR, +16);
            Date rewardDate = calendar.getTime();
            for (LockMarketExtraRewardConfig lockMarketExtraRewardConfig : extraRewardConfigs) {
                // 当月总业绩查询
                BigDecimal subPerformanceAmount =  lockMarketRewardDetailService
                        .getSubPerformanceAmountById(lockMarketExtraRewardConfig.getMemberId(),
                                DateUtil.dateToString(firstDayOfMounth), DateUtil.dateToString(lastDayOfMounth), lockMarketExtraRewardConfig.getSymbol());

                // 计算当月额外收益
                BigDecimal extraReward = subPerformanceAmount == null ? new BigDecimal(0) : subPerformanceAmount.multiply(new BigDecimal(lockMarketExtraRewardConfig.getRewardRate()));
                LockMarketRewardIncomePlan lockMarketRewardIncomePlan = new LockMarketRewardIncomePlan();
                lockMarketRewardIncomePlan.setRewardAmount(extraReward);
                lockMarketRewardIncomePlan.setRewardTime(rewardDate);
                lockMarketRewardIncomePlan.setMemberId(lockMarketExtraRewardConfig.getMemberId());
                lockMarketRewardIncomePlan.setComment("CNYT锁仓额外计划奖励");
                lockMarketRewardIncomePlan.setCreateTime(new Date());
                lockMarketRewardIncomePlan.setSymbol(lockMarketExtraRewardConfig.getSymbol());
                lockMarketRewardIncomePlan.setStatus(LockBackStatus.BACK);
                lockMarketRewardIncomePlan.setPeriod(1); // 返还期数为 1
                lockMarketRewardIncomePlan.setRewardType(LockRewardType.PREMIUMS);
                if(lockMarketRewardIncomePlan.getRewardAmount().compareTo(BigDecimal.ZERO) == 0) {
                    XxlJobLogger.log( "会员:" + lockMarketRewardIncomePlan.getMemberId() + "当月收益为0");
                } else {
                    lockMarketRewardIncomePlanService.save(lockMarketRewardIncomePlan);
                }

            }
            XxlJobLogger.log("计算有效配置会员的额外奖励 成功");
        } catch (Exception e) {
            e.printStackTrace();
            XxlJobLogger.log("执行CNYT额外奖励统计 失败:"  + "失败原因->" + e.getMessage());
        }
        return SUCCESS;
    }
}
