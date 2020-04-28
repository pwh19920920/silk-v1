package com.spark.bitrade.job;

import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * 生成各时间段的K线信息
 */
@Component
public class KLineGeneratorJob {
    @Autowired
    private CoinProcessorFactory processorFactory;

    /**
     * 每分钟定时器，处理分钟K线
     */
    @Scheduled(cron = "0 * * * * *")
    public void handle5minKLine() {
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            //processor.autoGenerate(); //生成1分钟K线，1min
            processor.generateKLine(1, Calendar.MINUTE, time); //1分钟=1min

            processor.update24HVolume(time); //更新当天24H成交量

            if (minute % 5 == 0) {
                processor.generateKLine(5, Calendar.MINUTE, time); //5分钟=5min
            }
            /*if(minute%10 == 0){
                processor.generateKLine(10, Calendar.MINUTE,time); //10分钟=10min
            }*/
            if (minute % 15 == 0) {
                processor.generateKLine(15, Calendar.MINUTE, time); //15分钟=15min
            }
            if (minute % 30 == 0) {
                processor.generateKLine(30, Calendar.MINUTE, time); //30分钟=30min
            }

            //每天凌晨重置当天缩略行情
            if (hour == 0 && minute == 0) {
                processor.resetThumb(); //重置Thumb
            }
        });
    }

    /**
     * 每小时运行，处理1小时k线
     */
    @Scheduled(cron = "0 0 * * * *")
    public void handleHourKLine() {
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            Calendar calendar = Calendar.getInstance();
            //将秒、微秒字段置为0
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTimeInMillis();

            processor.generateKLine(1, Calendar.HOUR_OF_DAY, time); //小时=1hour
        });
    }

    /**
     * 每日0点处理器，处理日K线、周k线、月K线
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void handleDayKLine() {
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            Calendar calendar = Calendar.getInstance();
            //将秒、微秒字段置为0
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTimeInMillis();

            int week = calendar.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            processor.generateKLine(1, Calendar.DAY_OF_YEAR, time); //日k线=1day
            if (week == 1) {
                processor.generateKLine(1, Calendar.DAY_OF_WEEK, time); //周k线=1week
            }
            if (dayOfMonth == 1) {
                processor.generateKLine(1, Calendar.DAY_OF_MONTH, time); //月k线=1month
            }
        });
    }


}
