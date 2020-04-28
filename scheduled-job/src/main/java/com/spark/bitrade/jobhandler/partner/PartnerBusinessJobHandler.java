package com.spark.bitrade.jobhandler.partner;


import com.spark.bitrade.dto.PartnerBusinessDto;
import com.spark.bitrade.dto.TempTable;
import com.spark.bitrade.entity.PartnerBusinessTemp;
import com.spark.bitrade.service.IDimAreaService;
import com.spark.bitrade.service.IMemberService;
import com.spark.bitrade.service.IPartnerBusinessTempService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import net.sf.ehcache.transaction.xa.EhcacheXAException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import javax.xml.crypto.Data;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.xxl.job.core.log.XxlJobLogger.log;

/**
 * 用于合伙人业务日表统计
 *
 * @author shenzucai
 * @time 2018.05.30 10:15
 */
@JobHandler(value = "partnerBusinessJobHandler")
@Component
@Transactional(rollbackOn = Exception.class)
public class PartnerBusinessJobHandler extends IJobHandler {

    @Autowired
    private IPartnerBusinessTempService iPartnerBusinessTempService;

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

        PartnerBusinessTemp partnerBusinessTemp = iPartnerBusinessTempService.getLastOfTable();
        // 数据库中最新统计周期
        Date staticDate = DateUtil.strToYYMMDDDate(partnerBusinessTemp.getStatisticalCycle());
        // 即将统计的周期的前一个周期
        Date yesStaticDate = DateUtil.strToYYMMDDDate(partnerBusinessDto.getPreDate());
        while(staticDate.compareTo(yesStaticDate) != 1){


            statisticalCycle = DateUtil.getDate(DateUtil.dateAddDay(staticDate, 1));
            partnerBusinessDto = new PartnerBusinessDto(statisticalCycle);

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);
            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)
            TempTable memberTable = new TempTable();
            memberTable.setBaseTable("member");
            memberTable.setNewTable("temp_member");
            Boolean aBoolean = iPartnerBusinessTempService.initTable(memberTable, partnerBusinessDto);
            XxlJobLogger.log("统计初始化表{0} {1}**********", memberTable.getNewTable(), aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

            TempTable exchangeOrderTable = new TempTable();
            exchangeOrderTable.setBaseTable("exchange_order");
            exchangeOrderTable.setNewTable("temp_exchange_order");
            aBoolean = iPartnerBusinessTempService.initTable(exchangeOrderTable, partnerBusinessDto);
            XxlJobLogger.log("统计初始化表{0} {1}**********", exchangeOrderTable.getNewTable(), aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)
            TempTable rewardRecordTable = new TempTable();
            rewardRecordTable.setBaseTable("reward_record");
            rewardRecordTable.setNewTable("temp_reward_record");
            aBoolean = iPartnerBusinessTempService.initTable(rewardRecordTable, partnerBusinessDto);
            XxlJobLogger.log("统计初始化表{0} {1}**********", rewardRecordTable.getNewTable(), aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }




            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)
            TempTable yesTodayPartBusiTable = new TempTable();
            yesTodayPartBusiTable.setBaseTable("partner_business");
            yesTodayPartBusiTable.setNewTable("yestody_partner_business");
            aBoolean = iPartnerBusinessTempService.initTable(yesTodayPartBusiTable, partnerBusinessDto);
            XxlJobLogger.log("统计初始化表{0} {1}**********", yesTodayPartBusiTable.getNewTable(), aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }





            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

            aBoolean = iPartnerBusinessTempService.deleteData("partner_business_temp") >= 0 ? true : false;
            XxlJobLogger.log("清理区域合伙人业务累计详细临时表{0} {1}**********", "partner_business_temp", aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }


            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);










            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

            partnerBusinessDto.setLevel(3);
            aBoolean = iPartnerBusinessTempService.saveListPartnerBusinessTemp(partnerBusinessDto) >= 0 ? true : false;
            XxlJobLogger.log("生成{0}区域合伙人业务累计详细临时表{1}数据{2}**********",partnerBusinessDto.getLevel() ,"partner_business_temp", aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

            partnerBusinessDto.setLevel(2);
            aBoolean = iPartnerBusinessTempService.saveListPartnerBusinessTemp(partnerBusinessDto) >= 0 ? true : false;
            XxlJobLogger.log("生成{0}区域合伙人业务累计详细临时表{1}数据{2}**********",partnerBusinessDto.getLevel() ,"partner_business_temp", aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)

            partnerBusinessDto.setLevel(1);
            aBoolean = iPartnerBusinessTempService.saveListPartnerBusinessTemp(partnerBusinessDto) >= 0 ? true : false;
            XxlJobLogger.log("生成{0}区域合伙人业务累计详细临时表{1}数据{2}**********",partnerBusinessDto.getLevel() ,"partner_business_temp", aBoolean ? "成功" : "失败");
            if (aBoolean == false) {
                return FAIL;
            }

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)









            partnerBusinessDto.setLevel(3);
            TempTable tempTable3 = new TempTable();
            tempTable3.setBaseTable("partner_business_temp");
            tempTable3.setNewTable("partner_business_temp3");
            XxlJobLogger.log("开始生成3级区域合伙人业务累计详细临时表****{0}******",tempTable3.getNewTable());
            aBoolean = iPartnerBusinessTempService.initPartnerLevelTmpeTable(tempTable3, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成3级区域合伙人业务累计详细临时表成功**********");

            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)


            partnerBusinessDto.setLevel(2);
            TempTable tempTable2 = new TempTable();
            tempTable2.setBaseTable("partner_business_temp3");
            tempTable2.setNewTable("partner_business_temp2");
            XxlJobLogger.log("开始生成2级区域合伙人业务累计详细临时表******{0}****",tempTable2.getNewTable());
            aBoolean = iPartnerBusinessTempService.initPartnerLevelTmpeTable(tempTable2, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成2级区域合伙人业务累计详细临时表成功**********");
            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)



            partnerBusinessDto.setLevel(1);
            TempTable tempTable1 = new TempTable();
            tempTable1.setBaseTable("partner_business_temp2");
            tempTable1.setNewTable("partner_business_temp1");
            XxlJobLogger.log("开始生成1级区域合伙人业务累计详细临时表****{0}******",tempTable1.getNewTable());
            aBoolean = iPartnerBusinessTempService.initPartnerLevelTmpeTable(tempTable1, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成1级区域合伙人业务累计详细临时表成功**********");
            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);










            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)



            XxlJobLogger.log("开始生成3级区域合伙人业务累计详细表**********");
            partnerBusinessDto.setLevel(3);
            tempTable3 = new TempTable();
            tempTable3.setBaseTable("partner_business");
            tempTable3.setNewTable("partner_business_temp3");
            aBoolean = iPartnerBusinessTempService.initPartnerBusinessTable(tempTable3, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成3级区域合伙人业务累计详细表成功**********");
            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)



            XxlJobLogger.log("开始生成2级区域合伙人业务累计详细表**********");
            partnerBusinessDto.setLevel(2);
            tempTable2 = new TempTable();
            tempTable2.setBaseTable("partner_business");
            tempTable2.setNewTable("partner_business_temp2");
            aBoolean = iPartnerBusinessTempService.initPartnerBusinessTable(tempTable2, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成2级区域合伙人业务累计详细表成功**********");
            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);

            batchStartTime = DateUtil.getTimeMillis();  //本批次开始运行时间(非业务需求)


            XxlJobLogger.log("开始生成1级区域合伙人业务累计详细表**********");
            partnerBusinessDto.setLevel(1);
            tempTable1 = new TempTable();
            tempTable1.setBaseTable("partner_business");
            tempTable1.setNewTable("partner_business_temp1");
            aBoolean = iPartnerBusinessTempService.initPartnerBusinessTable(tempTable1, partnerBusinessDto);
            if (aBoolean == false) {
                return FAIL;
            }
            XxlJobLogger.log("生成1级区域合伙人业务累计详细表成功**********");
            log("消耗{0}秒（累计消耗{1}秒）", (DateUtil.getTimeMillis() - batchStartTime)/1000, (DateUtil.getTimeMillis() - startTime)/1000);


            partnerBusinessTemp = iPartnerBusinessTempService.getLastOfTable();
            staticDate = DateUtil.strToYYMMDDDate(partnerBusinessTemp.getStatisticalCycle());
        }



        TimeUnit.SECONDS.sleep(2);
        return SUCCESS;
    }


}