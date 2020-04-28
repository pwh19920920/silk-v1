package com.spark.bitrade.controller.otc;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.annotation.ExcelSheet;
import com.spark.bitrade.annotation.IgnoreExcel;
import com.spark.bitrade.constant.AdminModule;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.controller.BaseController;
import com.spark.bitrade.entity.QMember;
import com.spark.bitrade.entity.QWithdrawRecord;
import com.spark.bitrade.model.screen.OrderScreen;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.QOrder;
import com.spark.bitrade.model.screen.OtcOrderExcelScreen;
import com.spark.bitrade.service.OrderService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.OtcOrderVO;
import jdk.nashorn.internal.ir.annotations.Ignore;
import net.sf.ehcache.store.compound.NullReadWriteCopyStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author rongyu
 * @description 法币交易订单
 * @date 2018/1/8 15:41
 */
@RestController
@RequestMapping("/otc/order")
public class AdminOrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @RequiresPermissions("otc:order:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.OTC, operation = "所有法币交易订单Order")
    public MessageResult all() {
        List<Order> exchangeOrderList = orderService.findAll();
        if (exchangeOrderList != null && exchangeOrderList.size() > 0) {
            return success(exchangeOrderList);
        }
        return error("没有数据");
    }

    @RequiresPermissions("otc:order-detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "法币交易订单Order详情")
    public MessageResult detail(Long id) {
        Order one = orderService.findOne(id);
        if (one == null) {
            return error("没有该ID数据");
        }
        return success(one);
    }

    //修改订单状态
    @RequiresPermissions("otc:order:alert-status")
    @PatchMapping("{id}/alert-status")
    @AccessLog(module = AdminModule.OTC, operation = "修改法币交易订单Order")
    public MessageResult status(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus status) {
        Order order = orderService.findOne(id);
        notNull(order, "validate order.id!");
        order.setStatus(status);
        orderService.save(order);
        return success();
    }


    @RequiresPermissions("otc:order-page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "分页查找法币交易订单Order")
    public MessageResult page(
            PageModel pageModel,
            OrderScreen screen) {
        //edit by yangch 时间： 2018.04.29 原因：合并
        //List<Predicate> predicate = getPredicate(screen);
        List<Predicate> predicate = getPredicates(screen);
        //add|edit|del by  shenzucai 时间： 2018.06.13  原因：区分分页查询和导出功能
        Page<OtcOrderVO> page = orderService.joinFind(predicate,pageModel);
        //Page<Order> all = orderService.findAll(predicate, pageModel.getPageable());
        return success(page);
    }

    private List<Predicate> getPredicates(OrderScreen screen) {
        ArrayList<Predicate> predicates = new ArrayList<>();
//        Order order = new Order();
//        predicates.add(QOrder.order.status.ne(OrderStatus.CANCELLED)); //add by yangch 时间： 2018.04.29 原因：合并
//        predicates.add(QOrder.order.advertiseId.gt(order.getAdvertiseId()));
        if (StringUtils.isNotBlank(screen.getOrderSn())){
            predicates.add(QOrder.order.orderSn.eq(screen.getOrderSn()));
        }
        if(screen.getAdvertiseId()!=null) {
            predicates.add(QOrder.order.advertiseId.eq(screen.getAdvertiseId()));//add by zyj 时间：2018/8/28 增加广告id，修复订单明细显示所有订单记录的问题
        }
        if (screen.getStartTime() != null){
            predicates.add(QOrder.order.createTime.goe(screen.getStartTime()));
        }
        if (screen.getEndTime() != null){
            predicates.add(QOrder.order.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }

        //add|edit|del by tansitao 时间： 2018/7/3 原因：增加释放时间查询
        if (screen.getReleaseStartTime() != null) {
            predicates.add(QOrder.order.releaseTime.goe(screen.getReleaseStartTime()));
        }
        if (screen.getReleaseEndTime() != null){
            predicates.add(QOrder.order.releaseTime.lt(DateUtil.dateAddDay(screen.getReleaseEndTime(),1)));
        }
        if (screen.getStatus() != null){
            predicates.add(QOrder.order.status.eq(screen.getStatus()));
        }
        //and by zyj 2018.11.30 增加是否手动取消的判断
        if (screen.getIsManualCancel() != null) {
            if (screen.getIsManualCancel() == BooleanEnum.IS_TRUE) {
                predicates.add(QOrder.order.isManualCancel.eq(screen.getIsManualCancel()));
            } else {
                predicates.add(QOrder.order.isManualCancel.eq(BooleanEnum.IS_FALSE).or(QOrder.order.isManualCancel.isNull()));
            }
        }
        if (StringUtils.isNotEmpty(screen.getUnit())){
            predicates.add(QOrder.order.coin.unit.equalsIgnoreCase(screen.getUnit()));
        }
        if (StringUtils.isNotBlank(screen.getMemberName())) {
            predicates.add(QOrder.order.memberName.like("%" + screen.getMemberName() + "%")
                    .or(QOrder.order.memberRealName.like("%" + screen.getMemberName() + "%")));
        }
        if (StringUtils.isNotBlank(screen.getCustomerName())) {
            predicates.add(QOrder.order.customerName.like("%" + screen.getCustomerName() + "%")
                    .or(QOrder.order.customerRealName.like("%" + screen.getCustomerName() + "%")));
        }
        if(screen.getMinMoney()!=null) {
            predicates.add(QOrder.order.money.goe(screen.getMinMoney()));
        }
        if(screen.getMaxMoney()!=null) {
            predicates.add(QOrder.order.money.loe(screen.getMaxMoney()));
        }
        if(screen.getMinNumber()!=null) {
            predicates.add(QOrder.order.number.goe(screen.getMinNumber()));
        }
        if(screen.getMaxNumber()!=null){
            predicates.add(QOrder.order.number.loe(screen.getMaxNumber()));
        }
        return /*PredicateUtils.getPredicate(booleanExpressions)*/predicates;
    }


    @RequiresPermissions("otc:order:get-order-num")
    @PostMapping("get-order-num")
    @AccessLog(module = AdminModule.OTC, operation = "后台首页订单总数接口")
    public MessageResult getOrderNum() {
        return orderService.getOrderNum();
    }

    /**
     * 参数 fileName 为导出excel 文件的文件名 格式为 .xls  定义在OutExcelInterceptor 拦截器中 ，非必须参数
     * @param pageModel
     * @param screen
     * @param response
     * @throws Exception
     */
    @RequiresPermissions("otc:order-out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "导出法币交易订单Order Excel")
    //edit by yangch 时间： 2018.04.29 原因：合并
    public void outExcel(
            PageModel pageModel,
            OrderScreen screen,
            HttpServletResponse response
    ) throws Exception {
        List<OtcOrderVO> list = orderService.outExcel(getPredicates(screen),pageModel);
        String fileName="otcOrder_"+DateUtil.dateToYYYYMMDDHHMMSS(new Date());
        ExcelUtil.listToCSV(list,OtcOrderVO.class.getDeclaredFields(),response,fileName);
//        ExcelUtil.listToExcel(list,OtcOrderVO.class.getDeclaredFields(),response.getOutputStream());
    }
    /*public void outExcel(
            PageModel pageModel,
            OtcOrderExcelScreen screen,
            HttpServletResponse response
            ) throws Exception {
        ArrayList<Predicate> predicates = new ArrayList<>();
        if (screen.getStartTime() != null)
            predicates.add(QOrder.order.createTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null)
            predicates.add(QOrder.order.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        if (screen.getStatus() != null)
            predicates.add(QOrder.order.status.eq(screen.getStatus()));
        if (StringUtils.isNotEmpty(screen.getUnit()))
            predicates.add(QOrder.order.coin.unit.equalsIgnoreCase(screen.getUnit()));
        if(screen.getMemberId()!=null)
            predicates.add(QOrder.order.memberId.eq(screen.getMemberId()));
        if(screen.getCustomerId()!=null)
            predicates.add(QOrder.order.customerId.eq(screen.getCustomerId()));
        List<OtcOrderVO> list = orderService.outExcel(predicates,pageModel).getContent();
        ExcelUtil.listToExcel(list,OtcOrderVO.class.getDeclaredFields(),OtcOrderVO.class.getAnnotation(ExcelSheet.class).size(),response.getOutputStream());
    }*/


}
