package com.spark.bitrade.jobhandler.stat;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.entity.WithdrawFeeStat;
import com.spark.bitrade.service.IWithdrawFeeStatService;
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
 * @time 2018.06.13 14:56
 */
@JobHandler(value = "withdrawFeeStatHandler")
@Component
public class WithdrawFeeStatHandler extends IJobHandler {

    @Autowired
    private IWithdrawFeeStatService iWithdrawFeeStatService;

    private Logger logger = LoggerFactory.getLogger(WithdrawFeeStatHandler.class);

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //核心处理逻辑描述：( 总提币手续统计数据 与 操盘账提币交易手续统计 逻辑相同 )
        //1.查询统计数据表是否已经存在总提币交易历史total手续费统计数据
        //2.若1中数据结果为true, 查询出每日总提币交易手续费统计数据,不包含total数据。
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
        String opDate =iWithdrawFeeStatService.getMaxOpDate();
        if(!StringUtils.isEmpty(opDate)){//如果存在最后统计日期
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            if(diffDay>0){//循环补足没有统计的日期
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    isSucc = withdrawFeeTotal(startTime,endTime);//总提币手续费统计
                    if(isSucc){
                        XxlJobLogger.log("总提币手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("总提币手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            isSucc = withdrawFeeTotal(startTime,endTime);//总提币手续费统计
            if(isSucc){
                XxlJobLogger.log("总提币手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("总提币手续费统计数据生成失败!>......................");
            }
        }


        opDate =iWithdrawFeeStatService.getTraderMaxOpDate();
        if(!StringUtils.isEmpty(opDate)){//如果存在最后统计日期
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            if(diffDay>0){//循环补足没有统计的日期
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    isSucc = traderWithdrawFeeTotal(startTime,endTime);//操盘手提币手续费统计
                    if(isSucc){
                        XxlJobLogger.log("操盘手提币手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("操盘手提币手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            isSucc = traderWithdrawFeeTotal(startTime,endTime);//操盘手提币手续费统计
            if(isSucc){
                XxlJobLogger.log("操盘手提币手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("操盘手提币手续费统计数据生成失败!>......................");
            }
        }

         return SUCCESS;
    }

    /**
    * 总提币手续费统计
    * @author fumy
    * @time 2018.06.14 13:37
     * @param
    * @return true
    */
    public boolean withdrawFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  iWithdrawFeeStatService.selectCount(new EntityWrapper<WithdrawFeeStat>().where("date={0}","total"));
        if(totalCount > 0){
            //获取每日总提币手续费统计数据
            List<WithdrawFeeStat> dayOfFeeStatList = iWithdrawFeeStatService.dayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList.size() >  totalCount){//每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                return initFeeStat(startTime,endTime);
            }

            //插入每日统计数据,成功插入后累加更新total数据
            for(int i=0;i<dayOfFeeStatList.size();i++){
                int row = iWithdrawFeeStatService.insertAndUpdate(dayOfFeeStatList.get(i));
                if(row > 0){
                    logger.info("保存每日总提币手续费统计数据成功");
                    int upRow = iWithdrawFeeStatService.updateTotal(dayOfFeeStatList.get(i));
                    if(upRow>0){
                        logger.info("更新总提币手续费统计数据成功！");
                        result = true;
                    }else {
                        logger.debug("更新总提币手续费统计数据失败！---> date=" +dayOfFeeStatList.get(i).getDate()+"---> unit=" + dayOfFeeStatList.get(i).getUnit());
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
        //初始化包含total的提币手续费统计数据
        List<WithdrawFeeStat> initFeeStatList = iWithdrawFeeStatService.listFeeStat(startTime,endTime);
        //将获取到的数据插入到总提币收费统计表
        boolean isSucc =true;
        int row =0;
        for(int i=0;i<initFeeStatList.size();i++){
            row = iWithdrawFeeStatService.insertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存总提币手续费统计数据成功！");
            }else {
                logger.debug("初始化保存总提币手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }


    /**
     * 操盘手提币手续费统计
     * @author fumy
     * @time 2018.06.14 13:37
     * @param
     * @return true
     */
    public boolean traderWithdrawFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  iWithdrawFeeStatService.selectTraderTotalCount();
        if(totalCount > 0){
            //获取每日总提币手续费统计数据
            List<WithdrawFeeStat> traderDayOfFeeStat = iWithdrawFeeStatService.traderDayOfFeeStat(startTime,endTime);
            if(traderDayOfFeeStat.size() >  totalCount){//每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                return initTraderFeeStat(startTime,endTime);
            }

            //插入每日统计数据,成功插入后累加更新total数据
            for(int i=0;i<traderDayOfFeeStat.size();i++){
                int row = iWithdrawFeeStatService.traderInsertAndUpdate(traderDayOfFeeStat.get(i));
                if(row > 0){
                    logger.info("保存每日操盘手提币手续费统计数据成功");
                    int upRow = iWithdrawFeeStatService.traderUpdateTotal(traderDayOfFeeStat.get(i));
                    if(upRow>0){
                        logger.info("更新操盘手提币手续费统计数据成功！");
                        result = true;
                    }else {
                        logger.debug("更新操盘手提币手续费统计数据失败！---> date=" +traderDayOfFeeStat.get(i).getDate()+"---> unit=" + traderDayOfFeeStat.get(i).getUnit());
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

    public  boolean initTraderFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的提币手续费统计数据
        List<WithdrawFeeStat> initTraderFeeStatList = iWithdrawFeeStatService.traderListFeeStat(startTime,endTime);
        //将获取到的数据插入到总提币收费统计表
        boolean isSucc =true;
        int row =0;
        for(int i=0;i<initTraderFeeStatList.size();i++){
            row = iWithdrawFeeStatService.traderInsertAndUpdate(initTraderFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存操盘手提币手续费统计数据成功！");
            }else {
                logger.debug("初始化保存操盘手提币手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }
}
