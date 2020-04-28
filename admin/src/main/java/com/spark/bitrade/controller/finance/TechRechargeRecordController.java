package com.spark.bitrade.controller.finance;

import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.dto.TechRechargeRecordDto;
import com.spark.bitrade.entity.C2cFeeStat;
import com.spark.bitrade.entity.QC2cFeeStat;
import com.spark.bitrade.entity.QTechRechargeRecord;
import com.spark.bitrade.entity.TechRechargeRecord;
import com.spark.bitrade.model.screen.C2cFeeStatScreen;
import com.spark.bitrade.model.screen.TechRechargeRecordScreen;
import com.spark.bitrade.service.C2cFeeStatService;
import com.spark.bitrade.service.C2cInnerFeeStatService;
import com.spark.bitrade.service.TechRechargeRecordService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.ExcelUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author shenzucai
 * @time 2018.06.20 16:44
 */
@RestController
@RequestMapping("/finance/finance-stat")
@Slf4j
public class TechRechargeRecordController extends BaseAdminController{

    @Autowired
    private TechRechargeRecordService techRechargeRecordService;


    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);


    /**技术充币
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
//    @RequiresPermissions("finance:finance-stat:techrechargerecord")
//    @PostMapping("techrechargerecord")
//    @AccessLog(module = AdminModule.FINANCE,operation = "技术充币")
//    public MessageResult exchangeFeeStat(PageModel pageModel, TechRechargeRecordScreen screen){
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getCoinUnit()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.coinUnit.eq(screen.getCoinUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getMemberAccount()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.memberAccount.eq(screen.getMemberAccount()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.createTime.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.createTime.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        Page<TechRechargeRecord> all = techRechargeRecordService.findAll(predicate, pageModel.getPageable());
//        logger.info("查询技术充币");
//        return success(all);
//    }



    /**技术充币
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param techRechargeRecordDto
     * @return true
     */
    @RequiresPermissions("finance:finance-stat-techrechargerecord:page-query")
    @PostMapping("techrechargerecord")
    @AccessLog(module = AdminModule.FINANCE,operation = "技术充币")
    public MessageResult exchangeFeeStat(PageModel pageModel, TechRechargeRecordDto techRechargeRecordDto){
        PageInfo<TechRechargeRecord> all = techRechargeRecordService.findByTechRechargeRecord(techRechargeRecordDto, pageModel);
        return success(all);
    }


    /**
     * 技术充币导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:finance-stat-techrechargerecord:out-excel")
    @GetMapping("techrechargerecord/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "技术充币导出")
    // edit by zyj: 时间：2018.10.10  重写导出
    public void exchangeFeeStatOutExcel(TechRechargeRecordDto techRechargeRecordDto,HttpServletResponse response){
        List<TechRechargeRecord> list=techRechargeRecordService.findByTechRechargeRecordOut(techRechargeRecordDto);
        String fileName="techRechargeRecord_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,TechRechargeRecord.class.getDeclaredFields(),response,fileName);
        logger.info("导出技术充币");
    }
//    public void exchangeFeeStatOutExcel(TechRechargeRecordScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getCoinUnit()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.coinUnit.eq(screen.getCoinUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getMemberAccount()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.memberAccount.eq(screen.getMemberAccount()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.createTime.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QTechRechargeRecord.techRechargeRecord.createTime.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<TechRechargeRecord> list = techRechargeRecordService.findAll(predicate);
//        String fileName="techRechargeRecord_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
//        ExcelUtil.listToCSV(list,TechRechargeRecord.class.getDeclaredFields(),response,fileName);
////        ExcelUtil.listToExcel(list,TechRechargeRecord.class.getDeclaredFields(),response.getOutputStream(),TechRechargeRecord.class.getName());
//        logger.info("导出技术充币");
//    }


}
