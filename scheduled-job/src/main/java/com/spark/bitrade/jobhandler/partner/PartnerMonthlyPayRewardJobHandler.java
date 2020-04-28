package com.spark.bitrade.jobhandler.partner;

import com.spark.bitrade.constant.PromotionRewardCycle;
import com.spark.bitrade.constant.RewardRecordStatus;
import com.spark.bitrade.constant.RewardRecordType;
import com.spark.bitrade.entity.RewardRecord;
import com.spark.bitrade.service.RewardRecordService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.xxl.job.core.log.XxlJobLogger.log;

/***
 * 合伙人每月佣金返佣定时任务
 * @author yangch
 * @time 2018.05.31 11:07
 */

@JobHandler(value="PartnerMonthlyPayRewardJobHandler")
@Component
public class PartnerMonthlyPayRewardJobHandler extends IJobHandler {

    @Autowired
    private RewardRecordService rewardRecordService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //核心处理逻辑描述：
        //1.根据条件更改返佣记录的状态未“发放中” ，并获取修改的记录总数，记录总数据量
        //3.分批次处理，循环处理数据：获取一批未处理的数据量，循环处理每条返佣记录
        //4  调用返佣发放的事务方法：
        //  4.1 增加用户余额；
        //  4.2 添加交易记录
        //  4.2 修改返佣记录（treated_time,status=2）
        // 5. 返佣失败则标记 返佣记录的状态为“3=发放失败”
        //其他逻辑：添加了处理进度的日志信息，消耗的时间等非业务逻辑


        log("-----合伙人每月佣金返佣定时任务-----begin");
        log("开始时间:{0}", DateUtil.getDateTime());
        ReturnT<String> returnT = new ReturnT<>(ReturnT.SUCCESS_CODE,"执行成功");

        Date firstDayOfCurrentMonth = DateUtil.strToYYMMDDDate(DateUtil.getDate(DateUtil.firstDayOfMonth())); //当月1日
        Date firstDayOfLastMonth = DateUtil.addMonth(firstDayOfCurrentMonth, -1);                         //上月1日

        boolean isCompleted = false;   //标记是否已发放完成
        int batchNo = 1;                //记录处理批次（非业务需求）
        int completedCount = 0;         //记录完成的进度（非业务需求）
        int intervalRecordCount = 500;    //日志打印的间隔记录条数(非业务需求)
        long startTime = DateUtil.getTimeMillis();  //开始运行时间(非业务需求)

        log("firstDayOfCurrentMonth:{0}",DateUtil.dateToString(firstDayOfCurrentMonth));
        log("firstDayOfLastMonth:{0}", DateUtil.dateToString(firstDayOfLastMonth));

        //标记返佣记录的状态为“发放中”
        //月返佣记录查询SQL：select * from reward_record where type=3 and status=0 and reward_cycle=3 and create_time>='2018-05-31 00:00:00' and create_time<'2018-06-01 00:00:00'
        int taskCount = rewardRecordService.preparePayReward(RewardRecordType.PARTNER, RewardRecordStatus.UNTREATED, PromotionRewardCycle.MONTHLY, firstDayOfLastMonth, firstDayOfCurrentMonth);
        log("taskCount:{0},消耗{1}秒", taskCount, (DateUtil.getTimeMillis() - startTime)/1000);

        //分批次返佣
        do {
            List<RewardRecord> list = rewardRecordService.findTopNByStatusAndTypeAndRewardCycle(RewardRecordStatus.TREATING, RewardRecordType.PARTNER, PromotionRewardCycle.MONTHLY);
            if(null == list || list.isEmpty()) {
                isCompleted = true;
            } else {
                log("第{0}批次，待发放记录数量为{1}条", batchNo++, list.size());
                long batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

                for (RewardRecord rewardRecord: list) {
                    //发放佣金业务逻辑
                    try {
                        rewardRecordService.payReward(rewardRecord);

                        //测试数据量小，模拟处理等待的效果
                        //TimeUnit.MILLISECONDS.sleep(2000);
                    } catch (Exception e) {
                        returnT.setCode(ReturnT.FAIL_CODE);
                        returnT.setMsg("有佣金发放失败");
                        log("有佣金发放失败，佣金记录：{0}",rewardRecord.toString());
                        log(e.getMessage());

                        try {
                            rewardRecordService.payRewardFailed(rewardRecord, RewardRecordStatus.TREATING); //标记发放失败的佣金记录
                        }catch (Exception ex){
                            log("标记佣金方式失败时出错了，详细信息：{0}",ex.getMessage());
                        }
                        e.printStackTrace();
                    }


                    //打印处理的进度(非业务需求)
                    if( completedCount++ % intervalRecordCount == 0) {
                        if(taskCount >= completedCount) {
                            int completedRate = completedCount*100/taskCount;
                            log("当前发放进度：{0}%({1}/{2})", completedRate, completedCount, taskCount);
                        } else {
                            log("已完成{0}", completedCount);
                        }
                    }
                }
                log("第{0}批次，处理{1}条记录，消耗{2}秒（累计消耗{3}秒）",
                        batchNo, list.size(),(DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);
            }
        } while (!isCompleted);


        log("完成时间:{0} , 消耗 {1} 秒", DateUtil.getDateTime(),(DateUtil.getTimeMillis() - startTime)/1000);
        log("-----合伙人每月佣金返佣定时任务-----end");

        return returnT;
    }

}
