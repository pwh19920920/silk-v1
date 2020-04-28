package com.spark.bitrade.jobhandler.stat;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.entity.ExchangeFeeStat;
import com.spark.bitrade.service.IExchangeFeeStatService;
import com.spark.bitrade.util.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author fumy
 * @time 2018.06.16 13:31
 */
@JobHandler(value = "exchangeFeeStatHandler")
@Component
public class ExchangeFeeStatHandler extends IJobHandler {

    @Autowired
    IExchangeFeeStatService iExchangeFeeStatService;

    private Logger logger = LoggerFactory.getLogger(ExchangeFeeStatHandler.class);

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //核心处理逻辑描述：( 总币币交易手续统计数据 与 操盘账户币币交易手续统计 逻辑相同 )
        //1.查询统计数据表是否已经存在总币币交易历史total手续费统计数据
        //2.若1中数据结果为true, 查询出每日总的币币交易手续费统计数据,不包含total数据。
        //  若1中数据结果为false,（跳过第三步） 直接执行初始化操作更新新增统计数据，不执行第五步。
        //3.判断每日统计数据条数是否大于total条数，如果大于，则执行初始化操作更新新增统计数据（覆盖更新所有total数据）
        //4.循环处理每一条统计数据，并返回处理结果
        //5.若返回成功，则更新并计算相同币种的total数据


        boolean isSucc;

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

        //获取数据统计的最后日期
        String opDate =iExchangeFeeStatService.getMaxOpDate();
        if(!StringUtils.isEmpty(opDate)){//如果存在最后统计日期
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            if(diffDay>0){//循环补足没有统计的日期
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    isSucc = exchangeFeeTotal(startTime,endTime);//总币币交易手续费统计
                    if(isSucc){
                        XxlJobLogger.log("总币币交易手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("总币币交易手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            isSucc = exchangeFeeTotal(startTime,endTime);//总币币交易手续费统计
            if(isSucc){
                XxlJobLogger.log("总币币交易手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("总币币交易手续费统计数据生成失败!>......................");
            }
        }

        opDate =iExchangeFeeStatService.getTraderMaxOpDate();
        if(!StringUtils.isEmpty(opDate)){//如果存在最后统计日期
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            if(diffDay>0){//循环补足没有统计的日期
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    isSucc = traderExchangeFeeTotal(startTime,endTime);//操盘账户币币交易手续费统计
                    if(isSucc){
                        XxlJobLogger.log("操盘账户币币交易手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("操盘账户币币交易手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            isSucc = traderExchangeFeeTotal(startTime,endTime);//操盘账户币币交易手续费统计
            if(isSucc){
                XxlJobLogger.log("操盘账户币币交易手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("操盘账户币币交易手续费统计数据生成失败!>......................");
            }
        }

        return SUCCESS;
    }


    /**
    * 总币币交易手续费统计
    * @author fumy
    * @time 2018.06.16 14:33
     * @param
    * @return true
    */
    public boolean exchangeFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  iExchangeFeeStatService.selectCount(new EntityWrapper<ExchangeFeeStat>().where("op_date={0}","total"));
        if(totalCount > 0){
            //获取每日总币币交易手续费统计数据
            List<ExchangeFeeStat> dayOfFeeStatList = iExchangeFeeStatService.dayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList.size() !=  totalCount){//每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                return initFeeStat(startTime,endTime);
            }

            //插入每日统计数据,成功插入后累加更新total数据
            for(int i=0;i<dayOfFeeStatList.size();i++){
                int row = iExchangeFeeStatService.insertAndUpdate(dayOfFeeStatList.get(i));
                if(row > 0){
                    logger.info("保存每日总币币交易手续费统计数据成功");
                    int upRow = iExchangeFeeStatService.updateTotal(dayOfFeeStatList.get(i));
                    if(upRow>0){
                        logger.info("更新总币币交易手续费统计数据成功！");
                        result = true;
                    }else {
                        logger.debug("更新总币币交易手续费统计数据失败！---> date=" +dayOfFeeStatList.get(i).getOpDate()+"---> unit=" + dayOfFeeStatList.get(i).getCoinUnit());
                        result = false;
                        break;
                    }
                }
            }
        }else{
            result = initFeeStat(startTime,endTime);
        }
        return  result;
    }

    /**
     * 初始化统计表数据
     * @author fumy
     * @time 2018.06.14 10:13
     * @param
     * @return true
     */
    public  boolean initFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的币币交易手续费统计数据
        List<ExchangeFeeStat> initFeeStatList = iExchangeFeeStatService.exchangeFeeTotal(startTime,endTime);
        //将获取到的数据插入到总币币交易收费统计表
        boolean isSucc =true;
        for(int i=0;i<initFeeStatList.size();i++){
            int row = iExchangeFeeStatService.insertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存总币币交易手续费统计数据成功！");
            }else {
                logger.debug("初始化保存总币币交易手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }

    /**
    * 操盘账户币币交易手续费统计
    * @author fumy
    * @time 2018.06.16 14:31
     * @param
    * @return true
    */
    public boolean traderExchangeFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  iExchangeFeeStatService.selectTraderTotalCount();
        if(totalCount > 0){
            //获取每日操盘账户币币交易手续费统计数据
            List<ExchangeFeeStat> dayOfFeeStatList = iExchangeFeeStatService.traderDayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList.size() !=  totalCount){//每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                return initTraderFeeStat(startTime,endTime);
            }

            //插入每日统计数据,成功插入后累加更新total数据
            for(int i=0;i<dayOfFeeStatList.size();i++){
                int row = iExchangeFeeStatService.traderInsertAndUpdate(dayOfFeeStatList.get(i));
                if(row > 0){
                    logger.info("保存每日操盘账户币币交易手续费统计数据成功");
                    int upRow = iExchangeFeeStatService.traderUpdateTotal(dayOfFeeStatList.get(i));
                    if(upRow>0){
                        logger.info("更新操盘账户币币交易手续费统计数据成功！");
                        result = true;
                    }else {
                        logger.debug("更新总币币交易手续费统计数据失败！---> date=" +dayOfFeeStatList.get(i).getOpDate()+"---> unit=" + dayOfFeeStatList.get(i).getCoinUnit());
                        result = false;
                        break;
                    }
                }
            }
        }else{
            result = initTraderFeeStat(startTime,endTime);
        }
        return  result;
    }

    /**
     * 初始化操盘账号币币交易手续费统计数据
     * @author fumy
     * @time 2018.06.16 14:54
     * @param
     * @return true
     */
    public  boolean initTraderFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的币币交易手续费统计数据
        List<ExchangeFeeStat> initFeeStatList = iExchangeFeeStatService.traderExchangeFeeTotal(startTime,endTime);
        //将获取到的数据插入到操盘手币币交易收费统计表
        boolean isSucc =true;
        for(int i=0;i<initFeeStatList.size();i++){
            int row = iExchangeFeeStatService.traderInsertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存操盘账户币币交易手续费统计数据成功！");
            }else {
                logger.debug("初始化保存操盘账户币币交易手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }
}
