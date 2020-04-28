package com.spark.bitrade.controller.finance;

import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.constant.TransactionType;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.CollectScreen;
import com.spark.bitrade.model.screen.MemberTransactionScreen;
import com.spark.bitrade.model.screen.WithdrawFeeStatScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MemberTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 交易记录
 * @date 2018/1/17 17:07
 */
@RestController
@RequestMapping("/finance/finance-stat")
@Slf4j
public class FinanceStatController extends BaseAdminController {


    @Autowired
    private WithdrawFeeStatService withdrawFeeStatService;

    @Autowired
    private TraderWithdrawFeeStatService traderWithdrawFeeStatService;


    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);


    /**
     * 总提币手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:withdrawfeestat-all-page-query")
    @PostMapping("withdrawfeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "总提币手续费")
    public MessageResult withdrawFeeStat(PageModel pageModel, WithdrawFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.unit.eq(screen.getUnit()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.time.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.time.lt(screen.getEndTime()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("date");
        directions.add(Sort.Direction.DESC);
        property.add("unit");
        directions.add(Sort.Direction.ASC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<WithdrawFeeStat> all = withdrawFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询总提笔手续费");
        return success(all);
    }


    /**
     * 总提币手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param pageModel
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:withdrawfeestat-all-out-excel")
    @GetMapping("withdrawfeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "总提币手续费导出")
    public void withdrawFeeStatOutExcel(PageModel pageModel, WithdrawFeeStatScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getUnit()))
//        {
//            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.unit.eq(screen.getUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.time.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QWithdrawFeeStat.withdrawFeeStat.time.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<WithdrawFeeStat> list = withdrawFeeStatService.findAll(predicate);
        //edit by zyj 2018-12-17: 重写导出
        List<WithdrawFeeStat> list = withdrawFeeStatService.findAllBy(screen.getUnit(),screen.getStartTime(),screen.getEndTime());
        String fileName="withdrawFeeStat_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,WithdrawFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,WithdrawFeeStat.class.getDeclaredFields(),response.getOutputStream(),WithdrawFeeStat.class.getName());
        logger.info("导出总提笔手续费");
    }


    /**
     * 操盘手提币手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:traderwithdrawfeestat-all-page-query")
    @PostMapping("traderwithdrawfeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "操盘手提币手续费")
    public MessageResult traderWithdrawFeeStat(PageModel pageModel, WithdrawFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.unit.eq(screen.getUnit()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.time.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.time.lt(screen.getEndTime()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("date");
        directions.add(Sort.Direction.DESC);
        property.add("unit");
        directions.add(Sort.Direction.ASC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<TraderWithdrawFeeStat> all = traderWithdrawFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询操盘手提笔手续费");
        return success(all);
    }


    /**
     * 操盘手提币手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param pageModel
     * @param screen
     * @param response
     * @return true
     */
    @RequiresPermissions("finance:finance-stat:traderwithdrawfeestat-all-out-excel")
    @GetMapping("traderwithdrawfeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "操盘手提币手续费导出")
    public void traderWithdrawFeeStatOutExcel(PageModel pageModel, WithdrawFeeStatScreen screen,HttpServletResponse response) throws IOException {
//        //条件
//        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
//        if (!StringUtils.isEmpty(screen.getUnit()))
//        {
//            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.unit.eq(screen.getUnit()));
//        }
//        if (!StringUtils.isEmpty(screen.getStartTime()))
//        {
//            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.time.gt(screen.getStartTime()));
//        }
//        if (!StringUtils.isEmpty(screen.getEndTime()))
//        {
//            booleanExpressions.add(QTraderWithdrawFeeStat.traderWithdrawFeeStat.time.lt(screen.getEndTime()));
//        }
//        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
//        List<TraderWithdrawFeeStat> list = traderWithdrawFeeStatService.findAll(predicate);
        //edit by zyj 2018-12-17: 重写导出
        List<TraderWithdrawFeeStat> list = traderWithdrawFeeStatService.findAllBy(screen.getUnit(),screen.getStartTime(),screen.getEndTime());
        String fileName="withdrawRecord_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,TraderWithdrawFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,TraderWithdrawFeeStat.class.getDeclaredFields(),response.getOutputStream(),TraderWithdrawFeeStat.class.getName());
        logger.info("导出操盘手提笔手续费");
    }


}
