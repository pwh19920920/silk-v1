package com.spark.bitrade.jobhandler;

import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.service.IOtcExchangeFeeAnyService;
import com.spark.bitrade.service.ISilkDataDistService;
import com.spark.bitrade.service.MemberService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 *  法币广告，BB交易手续费统计
 *    
 *  @author liaoqinghui  
 *  @time 2019.09.09 17:30  
 */
@JobHandler(value="otcExchangeFeeAnyJobHandler")
@Component
@Slf4j
public class OtcExchangeFeeAnyJobHandler extends IJobHandler {

    private static final String YYYYMMDD = "yyyy-MM-dd";
    private static final String DAYSTART = " 00:00:00";
    private static final String DAYEND = " 23:59:59";
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private IOtcExchangeFeeAnyService otcExchangeFeeAnyService;
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("=======================法币广告手续费,BB交易手续费,提币手续费统计===========================");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yester = DateUtil.dateToString(calendar.getTime(), YYYYMMDD);
        //获取昨天时间
        String yesterdayStart = yester + DAYSTART;
        String yesterdayEnd = yester + DAYEND;
        XxlJobLogger.log("=======================统计范围:"+yesterdayStart+"--"+yesterdayEnd+"===========================");
        SilkDataDist dataDist = silkDataDistService.findByIdAndKey("FEE_ADVERTISE_EXCHANGE", "ACCOUNT");
        if(dataDist==null){
            XxlJobLogger.log("=======================归集账号未配置ID:FEE_ADVERTISE_EXCHANGE,KEY:ACCOUNT===========================");
            return ReturnT.SUCCESS;
        }
        Long memberId = Long.valueOf(dataDist.getDictVal());
        Member one = memberService.findOne(memberId);
        if(one==null){
            XxlJobLogger.log("=======================归集账号不存在===========================");
            return ReturnT.SUCCESS;
        }
        otcExchangeFeeAnyService.run(yesterdayStart,yesterdayEnd,memberId);

        return ReturnT.SUCCESS;
    }



}




















