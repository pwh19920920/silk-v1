package com.spark.bitrade.controller.finance;

import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.model.screen.C2cFeeStatScreen;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.C2cFeeStatSynVO;
import io.swagger.annotations.*;
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
@Api(description = "C2C交易手续费查询",tags={"C2C交易手续费接口操作"})
@RestController
@RequestMapping("/finance/finance-stat")
@Slf4j
public class C2cFeeStatController extends BaseAdminController {


    @Autowired
    private C2cFeeStatService c2cFeeStatService;

    @Autowired
    private C2cInnerFeeStatService c2cInnerFeeStatService;

    @Autowired
    private C2cOuterFeeStatService c2cOuterFeeStatService;

    @Autowired
    private C2cFeeStatSynService c2cFeeStatSynService;

    private Logger logger = LoggerFactory.getLogger(BaseAdminController.class);


    /**
     * 总c2c交易手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    //@RequiresPermissions("finance:finance-stat:c2cfeestat-all")
    @PostMapping("c2cfeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "总c2c交易手续费")
    public MessageResult exchangeFeeStat(PageModel pageModel, C2cFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.unit.eq(screen.getUnit()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("opDate");
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<C2cFeeStat> all = c2cFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询总c2c交易手续费");
        return success(all);
    }


    /**
     * 总c2c交易手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    //@RequiresPermissions("finance:finance-stat:c2cfeestat-all:out-excel")
    @GetMapping("c2cfeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "总c2c交易手续费导出")
    public void exchangeFeeStatOutExcel(C2cFeeStatScreen screen,HttpServletResponse response) throws IOException {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cFeeStat.c2cFeeStat.unit.eq(screen.getUnit()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<C2cFeeStat> list = c2cFeeStatService.findAll(predicate);
        String fileName="c2cFeeStat_"+ DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,C2cFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,C2cFeeStat.class.getDeclaredFields(),response.getOutputStream(),C2cFeeStat.class.getName());
        logger.info("导出总c2c交易手续费");
    }


    /**
     * 内部商户c2c交易手续费
     * @author shenzucai
     * @time 2018.06.13 16:52
     * @param pageModel
     * @param screen
     * @return true
     */
    //@RequiresPermissions("finance:finance-stat:c2cinnerfeestat-all")
    @PostMapping("c2cinnerfeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "内部商户c2c交易手续费")
    public MessageResult traderExchangeFeeStat(PageModel pageModel, C2cFeeStatScreen screen){
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.unit.eq(screen.getUnit()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("opDate");
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<C2cInnerFeeStat> all = c2cInnerFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询内部商户c2c交易手续费");
        return success(all);
    }


    /**
     * 内部商户陈c2c手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    //@RequiresPermissions("finance:finance-stat:c2cinnerfeestat-all:out-excel")
    @GetMapping("c2cinnerfeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "内部商户c2c交易手续费导出")
    public void traderExchangeFeeStatOutExcel(C2cFeeStatScreen screen,HttpServletResponse response) throws IOException {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cInnerFeeStat.c2cInnerFeeStat.unit.eq(screen.getUnit()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<C2cInnerFeeStat> list = c2cInnerFeeStatService.findAll(predicate);
        String fileName="c2cInnerFeeStat_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,C2cInnerFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,C2cInnerFeeStat.class.getDeclaredFields(),response.getOutputStream(),C2cInnerFeeStat.class.getName());
        logger.info("导出内部商户c2c交易手续费");
    }


    /**
     * 外部商户c2c交易手续费
     * @author fumy
     * @time 2018.09.26 17:40
     * @param pageModel
     * @param screen
     * @return true
     */
    @ApiOperation(value = "外部商户c2c交易手续费",notes = "得到外部商户c2c交易手续费列表")
    @PostMapping("c2couterfeestat-all")
    @AccessLog(module = AdminModule.FINANCE,operation = "外部商户c2c交易手续费")
    public MessageRespResult<C2cOuterFeeStat> traderOuterFeeStat(@ApiParam(name ="pageModel",required = true)PageModel pageModel, @ApiParam(name ="screen",required = false) C2cFeeStatScreen screen){
//        MessageRespResult<C2cOuterFeeStat> result = new MessageRespResult<>();
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.unit.eq(screen.getUnit()));
        }
        ArrayList<Sort.Direction> directions=new ArrayList<>();
        List<String> property=new ArrayList<>();
        property.add("opDate");
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(property);
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<C2cOuterFeeStat> all = c2cOuterFeeStatService.findAll(predicate, pageModel.getPageable());
        logger.info("查询内部商户c2c交易手续费");
        return MessageRespResult.success("",all);
    }


    /**
     * 外部商户c2c手续费导出
     * @author shenzucai
     * @time 2018.06.13 15:07
     * @param screen
     * @param response
     * @return true
     */
    //@RequiresPermissions("finance:finance-stat:c2cinnerfeestat-all:out-excel")
    @GetMapping("c2couterfeestat-all/out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "外部商户c2c交易手续费导出")
    public void traderOutFeeStatOutExcel(C2cFeeStatScreen screen,HttpServletResponse response) throws IOException {
        //条件
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(screen.getType()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.type.eq(screen.getType()));
        }
        if (!StringUtils.isEmpty(screen.getStartTime()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.createTime.gt(screen.getStartTime()));
        }
        if (!StringUtils.isEmpty(screen.getEndTime()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.createTime.lt(screen.getEndTime()));
        }
        if (!StringUtils.isEmpty(screen.getUnit()))
        {
            booleanExpressions.add(QC2cOuterFeeStat.c2cOuterFeeStat.unit.eq(screen.getUnit()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        List<C2cOuterFeeStat> list = c2cOuterFeeStatService.findAll(predicate);
        String fileName="c2cOuterFeeStat_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,C2cOuterFeeStat.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,C2cOuterFeeStat.class.getDeclaredFields(),response.getOutputStream(),C2cOuterFeeStat.class.getName());
        logger.info("导出外部商户c2c交易手续费");
    }

    /**
     * C2C交易手续费综合查询分页
     * @author Zhang Yanjun
     * @time 2018.10.11 10:44
     * @param pageNo
     * @param pageSize
     * @param type
     * @param unit
     * @param startTime
     * @param endTime
    */
    @ApiOperation(value = "c2c交易手续费综合查询",notes = "c2c交易手续费综合查询分页列表")
    @PostMapping("c2cFeeStatSyn/page-query")
    @RequiresPermissions("finance:finance-stat-c2cFeeStatSyn:page-query")
    @AccessLog(module = AdminModule.FINANCE,operation = "c2c交易手续费综合查询分页")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNo", dataType = "int",required = true,paramType = "query"),
            @ApiImplicitParam(value = "页大小", name = "pageSize", dataType = "int",required = true,paramType = "query"),
            @ApiImplicitParam(value = "交易类型  0：买，1：卖", name = "type", dataType = "Integer"),
            @ApiImplicitParam(value = "币种", name = "unit", dataType = "String"),
            @ApiImplicitParam(value = "开始时间 如 2018-10-10", name = "startTime", dataType = "String"),
            @ApiImplicitParam(value = "结束时间 如 2018-10-10", name = "endTime", dataType = "String")
    })
    public MessageRespResult<C2cFeeStatSynVO> traderFeeStatSyn(int pageNo,int pageSize,Integer type,String unit,String startTime,String endTime){
        PageInfo<C2cFeeStatSynVO> pageInfo=c2cFeeStatSynService.findC2cFeeStatSyn(pageNo, pageSize,type,unit,startTime,endTime);
        return MessageRespResult.success("查询成功",PageData.toPageData(pageInfo));
    }

    /**
     * C2C交易手续费综合查询导出
     * @author Zhang Yanjun
     * @time 2018.10.11 10:44
     * @param type
     * @param unit
     * @param startTime
     * @param endTime
     */
    @ApiOperation(value = "c2c交易手续费综合查询导出",notes = "c2c交易手续费综合查询导出")
    @GetMapping("c2cFeeStatSyn/out-excel")
    @RequiresPermissions("finance:finance-stat-c2cFeeStatSyn:out-excel")
    @AccessLog(module = AdminModule.FINANCE,operation = "c2c交易手续费综合查询导出")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "交易类型  0：买，1：卖", name = "type", dataType = "Integer"),
            @ApiImplicitParam(value = "币种", name = "unit", dataType = "String"),
            @ApiImplicitParam(value = "开始时间 如 2018-10-10", name = "startTime", dataType = "String"),
            @ApiImplicitParam(value = "结束时间 如 2018-10-10", name = "endTime", dataType = "String")
    })
    public void traderFeeStatSynOut(Integer type,String unit,String startTime,String endTime,HttpServletResponse response){
        List<C2cFeeStatSynVO> list=c2cFeeStatSynService.findC2cFeeStatSynOut(type, unit, startTime, endTime);
        String fileName="c2cFeeStatSyn_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,C2cFeeStatSynVO.class.getDeclaredFields(),response,fileName);
        logger.info("导出C2C交易手续费综合查询");
    }

}
