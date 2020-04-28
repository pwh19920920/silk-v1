package com.spark.bitrade.service;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.constant.LockCoinRechargeThresholdType;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.constant.ProcessStatus;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.LockCoinRechargeSetting;
import com.spark.bitrade.entity.UnlockCoinTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 内部员工锁仓自动解锁监控服务

 * @author yangch
 * @time 2018.08.02 11:17
 */

@Slf4j
@Service
public class LockCoinRechargeMonitorService {
    @Autowired
    private LockCoinRechargeSettingService lockCoinRechargeSettingService;
    @Autowired
    private UnlockCoinTaskService unlockCoinTaskService;
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    //监控的解锁任务
    //List<LockCoinRechargeSettingDto> monitorTasks = null;
    Map<String, LockCoinRechargeSetting> monitorTasksMap = null;

    public void monitorTask(CoinThumb thumb){
        //1、初始化监控的数据
        this.initMonitorTasks();

        //2、处理监控结果
        this.processMonitorTasks(thumb);
    }

    //初始化任务
    private void initMonitorTasks(){
        if(null == monitorTasksMap) {
            monitorTasksMap = new HashMap<>();

            List<LockCoinRechargeSetting> monitorTasks = lockCoinRechargeSettingService.findAllValid();
            if(null == monitorTasks) {
                return;
            }

            //初始化前一次解锁价格
            monitorTasks.stream().forEach( e -> {
                UnlockCoinTask unlockCoinTask = unlockCoinTaskService.findOneNewly(e.getId());
                if(null != unlockCoinTask) {
                    e.setPrevUnlockPrice(unlockCoinTask.getPrice());
                }

                monitorTasksMap.put(e.getCoinSymbol()+"/USDT", e);
            });
        }
    }

    private void processMonitorTasks(CoinThumb thumb){
        if(null != monitorTasksMap
                && monitorTasksMap.containsKey(thumb.getSymbol())) {
            LockCoinRechargeSetting task = monitorTasksMap.get(thumb.getSymbol());
            //根据触发类型进行处理（目前只处理币的价值类型）
            if(task.getThresholdType() == LockCoinRechargeThresholdType.COIN_VALUE){
                //USDT价格
                BigDecimal usdtCny = coinExchangeRate.getUsdCnyRate();

                //当前币的价值
                BigDecimal currValue = thumb.getClose().multiply(usdtCny);
                //System.out.println("currValue="+currValue+",当前币价："+thumb.getClose()
                //        +",当前USDT的人民币汇率："+coinExchangeRate.getUsdCnyRate()+",上一次解锁触发价格:"+task.getPrevUnlockPrice());
                log.debug("currValue={},当前币价：{},当前USDT的人民币汇率：{},上一次解锁触发价格:{}" ,
                        currValue,thumb.getClose(),coinExchangeRate.getUsdCnyRate(), task.getPrevUnlockPrice());

                //变化量
                BigDecimal chgValue = currValue.subtract(task.getPrevUnlockPrice());
                if(chgValue.compareTo(BigDecimal.ZERO) < 0){
                    return;
                }

                //计算解锁次数
                int unlockTimes = chgValue.divide(task.getThresholdValue()).setScale(0, RoundingMode.DOWN).intValueExact();
                //System.out.println("unlockTimes=="+unlockTimes);
                log.debug("unlockTimes={}", unlockTimes);

                //币价每上涨到指定的阀值则自动解锁
                for (int i = 0; i < unlockTimes ; i++) {
                    //触发价
                    BigDecimal triggerPrice = task.getPrevUnlockPrice().add(task.getThresholdValue());

                    //触发币价解锁
                    UnlockCoinTask unlockCoinTask = new UnlockCoinTask();
                    unlockCoinTask.setRefActivitieId(task.getId());
                    unlockCoinTask.setType(LockType.HANDLE_LOCK);
                    unlockCoinTask.setStatus(ProcessStatus.NOT_PROCESSED);
                    unlockCoinTask.setPrice(triggerPrice);
                    unlockCoinTask.setCreateTime(new Date());
                    unlockCoinTask.setNote("解锁触发价为人民币价格，" + thumb.getSymbol() + "币价为" + thumb.getClose() + "，USDT价为" + usdtCny);
                    unlockCoinTaskService.save(unlockCoinTask);

                    //更新最近一次的解锁价格
                    task.setPrevUnlockPrice(triggerPrice);
                }
            } else {
                log.warn("不支持的自动解锁类型");
            }
        }
    }


}
