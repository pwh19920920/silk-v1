package com.spark.bitrade.jobhandler.stat;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.entity.C2cFeeStat;
import com.spark.bitrade.service.IC2cFeeStatService;
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
 * @time 2018.06.19 17:11
 */
@JobHandler(value = "c2cFeeStatHandler")
@Component
public class C2CFeeStatHandler extends IJobHandler{

    private Logger logger = LoggerFactory.getLogger(C2CFeeStatHandler.class);

    @Autowired
    IC2cFeeStatService ic2cFeeStatService;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //核心处理逻辑描述：( 总C2C的交易手续统计数据 与 内部商家交易手续统计 逻辑相同 )
        //1.查询统计数据表是否存在历史total总交易手续费统计数据
        //2.若1中数据结果为true, 查询出每日C2C总的交易手续费统计数据,不包含total数据。
        //  若1中数据结果为false,（跳过第三步） 直接执行初始化操作更新新增统计数据，不执C2C行第五步。
        //3.判断每日统计数据条数是否大于total条数，如果大于，则执行初始化操作更新新增统计数据（覆盖更新所有total数据）
        //4.循环处理每一条统计数据，并返回处理结果
        //5.若返回成功，则更新并计算相同币种的total数据
        boolean isSucc;

        Date nowDay = new Date();
        Date yesterday = DateUtil.addDay(nowDay,-1);
        String startTime = DateUtil.getDate(yesterday) +" 16:00:00";
        String endTime = DateUtil.getDate(new Date()) +" 16:00:00";
        Date endDate = DateUtil.stringToDate(endTime);
        //如果当天时间小于结束统计时间，则执行为前一天时间
        if(nowDay.getTime() < endDate.getTime()){
            endTime = DateUtil.getDate(yesterday) +" 16:00:00";
            yesterday = DateUtil.addDay(yesterday,-1);
            startTime = DateUtil.getDate(yesterday) +" 16:00:00";

        }

        //获取数据统计的最后日期
        String opDate =ic2cFeeStatService.getMaxOpDate();
        //如果存在最后统计日期
        if(!StringUtils.isEmpty(opDate)){
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            //循环补足没有统计的日期
            if(diffDay>0){
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    //总C2C交易手续费统计
                    isSucc = c2cFeeTotal(startTime,endTime);
                    if(isSucc){
                        XxlJobLogger.log("总C2C交易手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("总C2C易手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            //总C2C交易手续费统计
            isSucc = c2cFeeTotal(startTime,endTime);
            if(isSucc){
                XxlJobLogger.log("总C2C交易手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("总C2C易手续费统计数据生成失败!>......................");
            }
        }



        //获取数据统计的最后日期
        opDate = ic2cFeeStatService.getInnerMaxOpDate();
        //如果存在最后统计日期
        if(!StringUtils.isEmpty(opDate)){
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            //循环补足没有统计的日期
            if(diffDay>0){
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    //内部商家C2C交易手续费统计
                    isSucc = innerC2cFeeTotal(startTime,endTime);
                    if(isSucc){
                        XxlJobLogger.log("内部商家C2C交易手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("内部商家C2C交易手续费统计数据生成失败!>......................");
                    }
                }
            }

        }else {
            //内部商家C2C交易手续费统计
            isSucc = innerC2cFeeTotal(startTime,endTime);
            if(isSucc){
                XxlJobLogger.log("内部商家C2C交易手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("内部商家C2C交易手续费统计数据生成失败!>......................");
            }
        }

        //获取数据统计的最后日期
        opDate = ic2cFeeStatService.getOuterMaxOpDate();
        //如果存在最后统计日期
        if(!StringUtils.isEmpty(opDate)){
            //计算与当前日期天数差
            int diffDay = DateUtil.getDaysBetweenDate(opDate,endTime);
            //循环补足没有统计的日期
            if(diffDay>0){
                for(int i=0;i<diffDay;i++){
                    yesterday = DateUtil.addDay(DateUtil.stringToDate(opDate),i);
                    startTime = DateUtil.getDate(yesterday) +" 16:00:00";
                    Date today = DateUtil.addDay(yesterday,1);
                    endTime = DateUtil.getDate(today) +" 16:00:00";
                    //外部商家C2C交易手续费统计
                    isSucc = outerC2cFeeTotal(startTime,endTime);
                    if(isSucc){
                        XxlJobLogger.log("外部商家C2C交易手续费统计数据生成完毕!>......................");
                    }else {
                        XxlJobLogger.log("外部商家C2C交易手续费统计数据生成失败!>......................");
                    }
                }
            }
        }else {
            //外部商家C2C交易手续费统计
            isSucc = outerC2cFeeTotal(startTime,endTime);
            if(isSucc){
                XxlJobLogger.log("外部商家C2C交易手续费统计数据生成完毕!>......................");
            }else {
                XxlJobLogger.log("外部商家C2C交易手续费统计数据生成失败!>......................");
            }
        }


        return SUCCESS;
    }

    /**
     * 总C2C交易手续费统计
     * @author fumy
     * @time 2018.06.19 17:22
     * @param
     * @return true
     */
    public boolean c2cFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  ic2cFeeStatService.selectCount(new EntityWrapper<C2cFeeStat>().where("op_date={0}","total"));
        if(totalCount > 0){
            //获取每日总C2C交易手续费统计数据
            List<C2cFeeStat> dayOfFeeStatList = ic2cFeeStatService.dayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList == null || dayOfFeeStatList.size() == 0){
                XxlJobLogger.log("总C2C交易手续费{0}无统计数据!>......................",DateUtil.dateToStringDate(new Date()));
            }else {
                //每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                if (dayOfFeeStatList.size() > totalCount) {
                    return initFeeStat(startTime, endTime);
                }
                //插入每日统计数据,成功插入后累加更新total数据
                for (int i = 0; i < dayOfFeeStatList.size(); i++) {
                    int row = ic2cFeeStatService.insertAndUpdate(dayOfFeeStatList.get(i));
                    if (row > 0) {
                        logger.info("保存每日C2C交易手续费统计数据成功");
                        int upRow = ic2cFeeStatService.updateTotal(dayOfFeeStatList.get(i));
                        if (upRow > 0) {
                            logger.info("更新总C2C交易手续费统计数据成功！");
                            result = true;
                        } else {
                            logger.debug("更新总C2C交易手续费统计数据失败！---> date=" + dayOfFeeStatList.get(i).getOpDate() + "---> unit=" + dayOfFeeStatList.get(i).getType());
                            result = false;
                            break;
                        }
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
     * @time 2018.06.19 17:20
     * @param
     * @return true
     */
    public  boolean initFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的C2C交易手续费统计数据
        List<C2cFeeStat> initFeeStatList = ic2cFeeStatService.c2cFeeTotal(startTime,endTime);
        //将获取到的数据插入到总C2C交易收费统计表
        boolean isSucc =true;
        for(int i=0;i<initFeeStatList.size();i++){
            int row = ic2cFeeStatService.insertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存总C2C交易手续费统计数据成功！");
            }else {
                logger.debug("初始化保存总C2C交易手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }


    /**
     * 内部商家C2C交易手续费统计
     * @author fumy
     * @time 2018.06.19 17:24
     * @param
     * @return true
     */
    public boolean innerC2cFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  ic2cFeeStatService.innerFeeCount();
        if(totalCount > 0){
            //获取每日内部商家C2C交易手续费统计数据
            List<C2cFeeStat> dayOfFeeStatList = ic2cFeeStatService.innerC2cDayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList == null || dayOfFeeStatList.size() == 0){
                XxlJobLogger.log("内部C2C交易手续费{0}无统计数据!>......................",DateUtil.dateToStringDate(new Date()));
            }else {
                //每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                if(dayOfFeeStatList.size() >  totalCount){
                    return initInnerFeeStat(startTime,endTime);
                }

                //插入每日统计数据,成功插入后累加更新total数据
                for(int i=0;i<dayOfFeeStatList.size();i++){
                    int row = ic2cFeeStatService.innerInsertAndUpdate(dayOfFeeStatList.get(i));
                    if(row > 0){
                        logger.info("保存每日内部商家C2C交易手续费统计数据成功");
                        int upRow = ic2cFeeStatService.innerUpdateTotal(dayOfFeeStatList.get(i));
                        if(upRow>0){
                            logger.info("更新内部商家C2C交易手续费统计数据成功！");
                            result = true;
                        }else {
                            logger.debug("更新内部商家C2C交易手续费统计数据失败！---> date=" +dayOfFeeStatList.get(i).getOpDate()+"---> unit=" + dayOfFeeStatList.get(i).getType());
                            result = false;
                            break;
                        }
                    }
                }
            }
        }else{
            result = initInnerFeeStat(startTime,endTime);
        }
        return  result;
    }

    /**
     * 初始化内部商家C2C交易手续费统计数据
     * @author fumy
     * @time 2018.06.19 17:29
     * @param
     * @return true
     */
    public  boolean initInnerFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的内部商家C2C交易手续费统计数据
        List<C2cFeeStat> initFeeStatList = ic2cFeeStatService.innerC2cFeeTotal(startTime,endTime);
        //将获取到的数据插入到内部商家C2C交易收费统计表
        boolean isSucc =true;
        for(int i=0;i<initFeeStatList.size();i++){
            int row = ic2cFeeStatService.innerInsertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存内部商家C2C交易手续费统计数据成功！");
            }else {
                logger.debug("初始化保存内部商家C2C交易手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }

    /**
     * 外部商家
     * @author fumy
     * @time 2018.09.26 17:21
     * @param
     * @return true
     */
    public boolean outerC2cFeeTotal(String startTime,String endTime) throws Exception{
        boolean result = false;
        //查看统计表是否存在total数据
        int totalCount =  ic2cFeeStatService.outerFeeCount();
        if(totalCount > 0){
            //获取每日外部商家C2C交易手续费统计数据
            List<C2cFeeStat> dayOfFeeStatList = ic2cFeeStatService.outerC2cDayOfFeeStat(startTime,endTime);
            if(dayOfFeeStatList == null || dayOfFeeStatList.size() == 0){
                XxlJobLogger.log("外部商家C2C交易手续费{0}无统计数据!>......................",DateUtil.dateToStringDate(new Date()));
            }else {
                //每日统计数据条数大于total条数，执行初始化操作更新新增统计数据
                if(dayOfFeeStatList.size() >  totalCount){
                    result = initOuterFeeStat(startTime,endTime);
                }

                //插入每日统计数据,成功插入后累加更新total数据
                for(int i=0;i<dayOfFeeStatList.size();i++){
                    int row = ic2cFeeStatService.outerInsertAndUpdate(dayOfFeeStatList.get(i));
                    if(row > 0){
                        logger.info("保存每日外部商家C2C交易手续费统计数据成功");
                        int upRow = ic2cFeeStatService.outerUpdateTotal(dayOfFeeStatList.get(i));
                        if(upRow>0){
                            logger.info("更新外部商家C2C交易手续费统计数据成功！");
                            result = true;
                        }else {
                            logger.debug("更新外部商家C2C交易手续费统计数据失败！---> date=" +dayOfFeeStatList.get(i).getOpDate()+"---> unit=" + dayOfFeeStatList.get(i).getType());
                            result = false;
                            break;
                        }
                    }
                }
            }
        }else{
            result = initOuterFeeStat(startTime,endTime);
        }
        return  result;
    }

    /**
     * 初始化外部商家C2C交易手续费统计数据
     * @author fumy
     * @time 2018.09.26 17:21
     * @param
     * @return true
     */
    public  boolean initOuterFeeStat(String startTime,String endTime) throws Exception{
        //初始化包含total的外部商家C2C交易手续费统计数据
        List<C2cFeeStat> initFeeStatList = ic2cFeeStatService.outerC2cFeeTotal(startTime,endTime);
        //将获取到的数据插入到外部商家C2C交易收费统计表
        boolean isSucc =true;
        for(int i=0;i<initFeeStatList.size();i++){
            int row = ic2cFeeStatService.outerInsertAndUpdate(initFeeStatList.get(i));
            if(row >  0){
                logger.info("初始化保存外部商家C2C交易手续费统计数据成功！");
            }else {
                logger.debug("初始化保存外部商家C2C交易手续费统计数据失败！");
                isSucc = false;
                break;
            }
        }
        return isSucc;
    }
}
