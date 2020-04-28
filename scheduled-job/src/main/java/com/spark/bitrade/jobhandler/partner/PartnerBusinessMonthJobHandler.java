package com.spark.bitrade.jobhandler.partner;


import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.dto.TempTable;
import com.spark.bitrade.service.IPartnerBusinessMonthTempService;
import com.spark.bitrade.service.IPartnerBusinessTempService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.xxl.job.core.log.XxlJobLogger.log;

/**
 * 用于合伙人业务月表统计该定时器时间必须在日表定时器时间之后
 *
 * @author shenzucai
 * @time 2018.05.30 10:15
 */
@JobHandler(value = "PartnerBusinessMonthJobHandler")
@Component
@Transactional(rollbackOn = Exception.class)
public class PartnerBusinessMonthJobHandler extends IJobHandler {

    @Autowired
    private IPartnerBusinessTempService iPartnerBusinessTempService;

    @Autowired
    private IPartnerBusinessMonthTempService iPartnerBusinessMonthTempService;

    /**
     * 关于所有temp开头的表名，请不要修改
     *
     * @param param
     * @return true
     * @author shenzucai
     * @time 2018.05.31 17:17
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("统计初始化**********");
        log("开始时间:{0}", DateUtil.getDateTime());
        XxlJobLogger.log("初始化统计周期对象****使用北京时间*****");


        long startTime = DateUtil.getTimeMillis();  //开始运行时间(非业务需求)
        long batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        Date date = calendar.getTime();
        String statisticalCycle = DateUtil.getDate(DateUtil.dateAddDay(date, -1));
        PartnerBusinessDto partnerBusinessDto = new PartnerBusinessDto(statisticalCycle);
        XxlJobLogger.log("初始化统计周期对象完毕 {0}", partnerBusinessDto.toString());


        log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);
        batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)
        TempTable ptmTable = new TempTable();
        ptmTable.setBaseTable("partner_business");
        ptmTable.setNewTable("partner_business_month_temp");
        Boolean aBoolean = iPartnerBusinessTempService.initTable(ptmTable, partnerBusinessDto);
        XxlJobLogger.log("统计初始化表{0} {1}**********", ptmTable.getNewTable(), aBoolean ? "成功" : "失败");
        if (aBoolean == false) {
            return FAIL;
        }

        log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);




        batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)
        aBoolean = iPartnerBusinessMonthTempService.deleteDataByStatisticalCycleAndLevel(partnerBusinessDto)>=0?true:false;
        XxlJobLogger.log("清理月表中周期为{0}的数据 {1} 如果存在的话 ", partnerBusinessDto.getDate(), aBoolean ? "成功" : "失败");
        if (aBoolean == false) {
            return FAIL;
        }
        aBoolean = iPartnerBusinessMonthTempService.savePartnerBusinessMonth(partnerBusinessDto)>=0;
        XxlJobLogger.log("插入月表中周期为{0}的数据 {1}", partnerBusinessDto.getDate(), aBoolean ? "成功" : "失败");
        if (aBoolean == false) {
            return FAIL;
        }
        log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);




        TimeUnit.SECONDS.sleep(2);
        return SUCCESS;
    }


}