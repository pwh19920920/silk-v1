package com.spark.bitrade.jobhandler;

import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.annotation.MybatisTransactional;
import com.spark.bitrade.service.impl.LockHqbComputeIncomeService;
import com.spark.bitrade.service.impl.LockHqbConfirmIntoService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Slf4j
@JobHandler(value = "hqbConfirmAndIncomeHandler")
@Component
public class HqbConfirmAndIncomeHandler extends IJobHandler {

    @Autowired
    private LockHqbConfirmIntoService lockHqbConfirmIntoService;
    @Autowired
    private LockHqbComputeIncomeService computeIncomeService;

    @Override
    @MybatisTransactional(rollbackFor = Exception.class)
    public ReturnT<String> execute(String s) throws Exception {
        try {
            JSONObject config = StringUtils.isNotBlank(s) ? JSONObject.parseObject(s) : new JSONObject();
            // 获取参数
            String type = config.containsKey("type") ? config.getString("type") : "";
            int hour = config.containsKey("hour") ? config.getIntValue("hour") : 15;
            Long appId = config.containsKey("appId") ? config.getLong("appId") : null;
            String coinSymbol = config.containsKey("coinSymbol") ? config.getString("coinSymbol") : null;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, calendar.get(Calendar.HOUR_OF_DAY) < hour ? -1 : 0);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 开始定时器
            log.info("开始定时任务：{} {}", appId, coinSymbol);
            if ("income".equalsIgnoreCase(type)) {
                XxlJobLogger.log("[beg] -> 活期宝收益计算...");
                computeIncomeService.computeIncome(calendar, appId, coinSymbol);
                XxlJobLogger.log("[end] -> 活期宝收益计算...");
            } else if ("confirm".equalsIgnoreCase(type)) {
                // 确认前一天15点之前的转入
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                XxlJobLogger.log("[beg] -> 活期宝自动确认...");
                lockHqbConfirmIntoService.confirmInto(calendar, appId, coinSymbol);
                XxlJobLogger.log("[end] -> 活期宝自动确认...");
            } else if ("transfer".equals(type)) {
                // 批量转出
                XxlJobLogger.log("[beg] -> 活期宝批量转出...");
                computeIncomeService.batchTransfer(coinSymbol);
                XxlJobLogger.log("[end] -> 活期宝批量转出...");
            }
        } catch (Exception e) {
            log.info("活期宝定时任务执行异常", e);
            XxlJobLogger.log(e);
            return FAIL;
        }
        return SUCCESS;
    }
}
