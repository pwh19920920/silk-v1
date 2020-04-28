package com.spark.bitrade.jobhandler.stat;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.entity.FincPlatStat;
import com.spark.bitrade.service.IFincPlatStatService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author shenzucai
 * @time 2018.06.13 08:40
 */
@JobHandler(value="fincPlatStatJobHandler")
@Component
public class FincPlatStatJobHandler extends IJobHandler {

    @Autowired
    private IFincPlatStatService iFincPlatStatService;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Logger logger = LoggerFactory.getLogger(FincPlatStatJobHandler.class);
    @Override
    public ReturnT<String> execute(String s) throws Exception {

        Calendar calendar = new GregorianCalendar();
        Date date = new Date();
        calendar.setTime(date);
//        calendar.add(Calendar.DATE,-1);
        logger.info("统计数据至------------{}",calendar.getTime());

        Date nowDay = new Date();
        Date yesterday = DateUtil.addDay(nowDay,-1);
        String startTime = DateUtil.getDate(yesterday) +" 16:00:00";
        String endTime = DateUtil.getDate(new Date()) +" 16:00:00";
        Date endDate = DateUtil.stringToDate(endTime);
        if(nowDay.getTime() < endDate.getTime()){//如果当天时间小于结束统计时间，则执行为前一天时间
            endTime = DateUtil.getDate(yesterday) +" 16:00:00";
            yesterday = DateUtil.addDay(yesterday,-1);
            startTime = DateUtil.getDate(yesterday) +" 16:00:00";

        }

        // 1，获取平台统计表最新数据
        List<FincPlatStat> fincPlatStatList = iFincPlatStatService.listPlatStat(startTime,endTime);
        //2，对表中的历史平台数据进行清除，不包括每天的数据
        Boolean isDelete = iFincPlatStatService.delete(new EntityWrapper<FincPlatStat>()
                .where("date={0}","total"));
        if(isDelete){
            Boolean isSave = true;
            //3,将新数据追加到表中
            for(FincPlatStat fincPlatStat:fincPlatStatList){
                FincPlatStat fincPlatStat1 = iFincPlatStatService.selectOne(new EntityWrapper<FincPlatStat>()
                        .where("date={0}",SIMPLE_DATE_FORMAT.format(calendar.getTime())).and("unit={0}",fincPlatStat.getUnit()));

                if(fincPlatStat1 == null || "total".equalsIgnoreCase(fincPlatStat.getDate())) {
                    isSave = iFincPlatStatService.insert(fincPlatStat);
                }
            }
            if(isSave){
                logger.info("生成平台统计表数据成功");
            }else{
                return FAIL;
            }
        }else{
            return FAIL;
        }
        return SUCCESS;
    }
}
