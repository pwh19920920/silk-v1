package com.spark.bitrade.controller.exchange;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.spark.bitrade.annotation.AccessLog;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.controller.common.BaseAdminController;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.model.screen.ExchangeOrderScreen;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.service.ExchangeOrderService;
import com.spark.bitrade.util.FileUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.util.PredicateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rongyu
 * @description
 * @date 2018/1/31 10:52
 */
@RestController
@RequestMapping("exchange/exchange-order")
public class ExchangeOrderController extends BaseAdminController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @RequiresPermissions("exchange:exchange-order:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查找所有exchangeOrder")
    public MessageResult all() {
        List<ExchangeOrder> exchangeOrderList = exchangeOrderService.findAll();
        if (exchangeOrderList != null && exchangeOrderList.size() > 0)
            return success(exchangeOrderList);
        return error("没有数据");
    }

    @RequiresPermissions("exchange:exchange-order:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "exchangeOrder详情")
    public MessageResult detail(String id,int pageNo,int pageSize) {
//        List<ExchangeOrderDetail> one = exchangeOrderService.getAggregation(id);
//        if (one == null)
//            return error("没有该ID数据");
//        return success(one);
        //mongodb分页
        Sort.Order order = new Sort.Order(Sort.Direction.DESC,"time");
        Sort sort = new Sort(order);
        Query query = new Query();
        Criteria criteria = Criteria.where("orderId").is(id.trim());
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query,ExchangeOrderDetail.class,"exchange_order_detail");
        //分页查询mongo从0开始
        PageRequest page = new PageRequest(pageNo, pageSize,sort);
        query.with(page);
        List<ExchangeOrderDetail> result=mongoTemplate.find(query,ExchangeOrderDetail.class,"exchange_order_detail");
        PageInfo<ExchangeOrderDetail> pageInfo = new PageInfo<>();

        //计算分页数据
        pageInfo.setTotal(count);
        pageInfo.setList(result);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(PageData.pageNo4PageHelper(pageNo));
        pageInfo.setPages( ((int)count + pageSize )/pageSize) ;
        return success(PageData.toPageData(pageInfo));
    }

    @RequiresPermissions("exchange:exchange-order:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "分页查找exchangeOrder")
    public MessageResult page(
            PageModel pageModel,
            ExchangeOrderScreen screen) {
        if (pageModel.getDirection() == null && pageModel.getProperty() == null) {
            ArrayList<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setDirection(directions);
            List<String> property = new ArrayList<>();
            property.add("orderId"); //edit by zyj
            pageModel.setProperty(property);
        }
        //获取查询条件
        Predicate predicate = getPredicate(screen);
        Page<ExchangeOrder> all = exchangeOrderService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    private Predicate getPredicate(ExchangeOrderScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QExchangeOrder qExchangeOrder = QExchangeOrder.exchangeOrder;
        if (screen.getOrderDirection() != null)
            booleanExpressions.add(qExchangeOrder.direction.eq(screen.getOrderDirection()));
        if (StringUtils.isNotEmpty(screen.getOrderId()))
            booleanExpressions.add(qExchangeOrder.orderId.eq(screen.getOrderId()));
        if (screen.getMemberId() != null)
            booleanExpressions.add(qExchangeOrder.memberId.eq(screen.getMemberId()));
        if (screen.getType() != null)
            booleanExpressions.add(qExchangeOrder.type.eq(screen.getType()));
        if (StringUtils.isNotBlank(screen.getCoinSymbol()))
            booleanExpressions.add(qExchangeOrder.coinSymbol.equalsIgnoreCase(screen.getCoinSymbol()));
        if (StringUtils.isNotBlank(screen.getBaseSymbol()))
            booleanExpressions.add(qExchangeOrder.baseSymbol.equalsIgnoreCase(screen.getBaseSymbol()));
        if (screen.getStatus() != null)
            booleanExpressions.add(qExchangeOrder.status.eq(screen.getStatus()));
        if (screen.getMinPrice()!=null)
            booleanExpressions.add(qExchangeOrder.price.goe(screen.getMinPrice()));
        if (screen.getMaxPrice()!=null)
            booleanExpressions.add(qExchangeOrder.price.loe(screen.getMaxPrice()));
        if (screen.getMinTradeAmount()!=null)
            booleanExpressions.add(qExchangeOrder.tradedAmount.goe(screen.getMinTradeAmount()));
        if (screen.getMaxTradeAmount()!=null)
            booleanExpressions.add(qExchangeOrder.tradedAmount.loe(screen.getMaxTradeAmount()));
        if (screen.getMinTurnOver()!=null)
            booleanExpressions.add(qExchangeOrder.turnover.goe(screen.getMinTurnOver()));
        if (screen.getMaxTurnOver()!=null)
            booleanExpressions.add(qExchangeOrder.turnover.loe(screen.getMaxTurnOver()));
        if(screen.getCompleted()!=null)
            /**
             * 委托订单
             */
            if(screen.getCompleted()== BooleanEnum.IS_FALSE){
                booleanExpressions.add(qExchangeOrder.status.eq(ExchangeOrderStatus.TRADING));
            }else{
                /**
                 * 历史订单
                 */
                booleanExpressions.add(qExchangeOrder.status.gt(ExchangeOrderStatus.TRADING));
            }
        return PredicateUtils.getPredicate(booleanExpressions);
    }


    @RequiresPermissions("exchange:exchange-order:out-excel")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "导出 exchangeOrder Excel")
    public MessageResult outExcel(
            @RequestParam(value = "memberId") Long memberId,
            @RequestParam(value = "type") ExchangeOrderType type,
            @RequestParam(value = "symbol") String symbol,
            @RequestParam(value = "status") ExchangeOrderStatus status,
            @RequestParam(value = "direction") ExchangeOrderDirection direction,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取查询条件
        List<Predicate> predicates = getPredicates(memberId, type, symbol, status, direction);
        List list = exchangeOrderService.queryWhereOrPage(predicates, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "order");
    }

    //查询条件的获取
    public List<Predicate> getPredicates(Long memberId, ExchangeOrderType type, String symbol, ExchangeOrderStatus status, ExchangeOrderDirection direction) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        QExchangeOrder qExchangeOrder = QExchangeOrder.exchangeOrder;
        //predicates.add(qExchangeOrder.symbol.eq(QExchangeCoin.exchangeCoin.symbol));
        if (memberId != null)
            predicates.add(qExchangeOrder.memberId.eq(memberId));
        if (type != null)
            predicates.add(qExchangeOrder.type.eq(type));
        if (symbol != null)
            predicates.add(qExchangeOrder.symbol.eq(symbol));
        if (status != null)
            predicates.add(qExchangeOrder.status.eq(status));
        if (direction != null)
            predicates.add(qExchangeOrder.direction.eq(direction));
        return predicates;
    }

    @RequiresPermissions("exchange:exchange-order:cancel")
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "取消委托")
    public MessageResult cancelOrder(String orderId) {
        ExchangeOrder order = exchangeOrderService.findOne(orderId);
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "order not in trading");
        }
        // 发送消息至Exchange系统
        kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
        return MessageResult.success("submit success");
    }
}
