package com.spark.bitrade.controller.finance;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.ExchangeFeeStatScreen;
import com.spark.bitrade.service.ExchangeFeeStatService;
import com.spark.bitrade.service.TraderExchangeFeeStatService;
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
import org.springframework.data.domain.Sort;
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
 * @author rongyu
 * @description 交易记录
 * @date 2018/1/17 17:07
 */
@RestController
@RequestMapping("/finance/finance-stat")
@Slf4j
public class ExchangeFeeStatController extends BaseAdminController {


    @Autowired
    private ExchangeFeeStatService exchangeFeeStatService;

    @Autowired
    private TraderExchangeFeeStatService traderExchangeFeeStatService;


    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);


    /**
     * 总币币交易手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:exchangefeestat-all-page-query")
    @PostMapping("exchangefeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "总币币交易手续费")
    public MessageResult exchangeFeeStat(PageModel pageModel, ExchangeFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getBaseUnit()))
        {
            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.baseUnit.eq(screen.getBaseUnit()));
        }
        if (!StringUtils.isEmpty(screen.getCoinUnit()))
        {
            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.coinUnit.eq(screen.getCoinUnit()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.createTime.lt(screen.getEndTime()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("opDate");
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<ExchangeFeeStat> all = exchangeFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询总币币交易手续费");
        return success(all);
    }


    /**
     * 总币币交易手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:exchangefeestat-all-out-excel")
    @GetMapping("exchangefeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "总币币交易手续费导出")
    public void exchangeFeeStatOutExcel(ExchangeFeeStatScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getBaseUnit()))
//        {
//            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.baseUnit.eq(screen.getBaseUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getCoinUnit()))
//        {
//            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.coinUnit.eq(screen.getCoinUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.createTime.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QExchangeFeeStat.exchangeFeeStat.createTime.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<ExchangeFeeStat> list = exchangeFeeStatService.findAll(predicate);
        //edit by zyj 2018-12-17: 重写导出
        List<ExchangeFeeStat> list = exchangeFeeStatService.findAllBy(screen.getBaseUnit(),screen.getCoinUnit(),screen.getStartTime(),screen.getEndTime());
        String fileName="exchangeFeeStat_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,ExchangeFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,ExchangeFeeStat.class.getDeclaredFields(),response.getOutputStream(),ExchangeFeeStat.class.getName());
        logger.info("导出总币币交易手续费");
    }


    /**
     * 操盘手提币手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:traderexchangefeestat-all-page-query")
    @PostMapping("traderexchangefeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "操盘手币币交易手续费")
    public MessageResult traderExchangeFeeStat(PageModel pageModel, ExchangeFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getBaseUnit()))
        {
            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.baseUnit.eq(screen.getBaseUnit()));
        }
        if (!StringUtils.isEmpty(screen.getCoinUnit()))
        {
            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.coinUnit.eq(screen.getCoinUnit()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.createTime.lt(screen.getEndTime()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("opDate");
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<TraderExchangeFeeStat> all = traderExchangeFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询操盘手币币交易手续费");
        return success(all);
    }


    /**
     * 操盘手提币手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:traderexchangefeestat-all-out-excel")
    @GetMapping("traderexchangefeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "操盘手币币交易手续费导出")
    public void traderExchangeFeeStatOutExcel(ExchangeFeeStatScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getBaseUnit()))
//        {
//            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.baseUnit.eq(screen.getBaseUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getCoinUnit()))
//        {
//            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.coinUnit.eq(screen.getCoinUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.createTime.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QTraderExchangeFeeStat.traderExchangeFeeStat.createTime.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<TraderExchangeFeeStat> list = traderExchangeFeeStatService.findAll(predicate);
        //edit by zyj 2018-12-17: 重写导出
        List<TraderExchangeFeeStat> list = traderExchangeFeeStatService.findAllBy(screen.getBaseUnit(),screen.getCoinUnit(),screen.getStartTime(),screen.getEndTime());
        String fileName="traderExchangeFeeStat_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,TraderExchangeFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,TraderExchangeFeeStat.class.getDeclaredFields(),response.getOutputStream(),TraderExchangeFeeStat.class.getName());
        logger.info("导出操盘手币币交易手续费");
    }


}
