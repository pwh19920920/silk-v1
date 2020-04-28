package com.spark.bitrade.service;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.dao.OrderDetailAggregationRepository;
import com.spark.bitrade.dto.PageParam;
import com.spark.bitrade.dto.Pagenation;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.entity.ExchangeTrade;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.OrderDetailAggregation;
import com.spark.bitrade.constant.OrderTypeEnum;
import com.spark.bitrade.service.base.MongoBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OrderDetailAggregationService extends MongoBaseService<OrderDetailAggregation> {

    @Autowired
    private OrderDetailAggregationRepository orderDetailAggregationRepository;

    @Autowired
    private MemberService memberService;


    //@Transactional(rollbackFor = Exception.class) 不支持事物
    public OrderDetailAggregation save(OrderDetailAggregation aggregation) {
        return orderDetailAggregationRepository.save(aggregation);
    }

    /**
     *
     * @param order 订单信息
     * @param trade 交易信息
     * @param member 会员信息
     * @param fee  手续费
     * @param feeDiscount  优惠的手续费
     * @return
     */
    @Async("mongodb")
    public OrderDetailAggregation asncySaveOrderDetailAggregation(final ExchangeOrder order ,final ExchangeTrade trade,
                                                                  final Member member, final BigDecimal fee, final BigDecimal feeDiscount) {
        String refOrderId = trade.getSellOrderId();  //关联订单号
        if(order.getOrderId().equalsIgnoreCase( refOrderId )) {
            refOrderId = trade.getBuyOrderId();
        }

        OrderDetailAggregation aggregation = new OrderDetailAggregation();
        aggregation.setType(OrderTypeEnum.EXCHANGE);
        ///aggregation.setAmount(order.getAmount().doubleValue());
        aggregation.setAmount(trade.getAmount().doubleValue()); //edit by yangch 时间： 2018.06.09 原因：不能使用订单的数量，需要使用交易的数量
        aggregation.setFee(fee.doubleValue());
        aggregation.setFeeDiscount(feeDiscount.doubleValue());
        aggregation.setTime(Calendar.getInstance().getTimeInMillis());
        aggregation.setDirection(order.getDirection());
        aggregation.setOrderId(order.getOrderId());
        aggregation.setRefOrderId(refOrderId);
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            aggregation.setUnit(order.getBaseSymbol());
        } else {
            aggregation.setUnit(order.getCoinSymbol());
        }
        //Member member = memberService.findOne(order.getMemberId());
        if (member != null) {
            aggregation.setMemberId(member.getId());
            aggregation.setUsername(member.getUsername());
            aggregation.setRealName(member.getRealName());
        }

        return orderDetailAggregationRepository.save(aggregation);
    }

    /***
      * 删除指定的成交详情
      *
      * @author yangch
      * @time 2018.06.09 10:37 
     * @param orderId 订单号
     * @param refOrderId 关联订单号
     */
    public boolean deleteByOrderIdAndRefOrderId(String orderId, String refOrderId){
        if(orderDetailAggregationRepository.deleteOrderDetailAggregationByOrderIdAndRefOrderId(orderId, refOrderId)>0){
            return true;
        } else {
            return  false;
        }
    }

    public Pagenation<OrderDetailAggregation> getDetail(PageParam pageParam, Long memberId, String coinName, OrderTypeEnum orderType) {

        Criteria criteria = new Criteria();
        criteria.where("1").equals("1");
        if (memberId != 0)
            criteria.and("memberId").is(memberId);
        if (orderType != null)
            criteria.and("type").is(orderType);
        if (!StringUtils.isEmpty(coinName))
            criteria.and("coinName").is(coinName);
        Query query = new Query(criteria);
        return page(pageParam, query, OrderDetailAggregation.class, "order_detail_aggregation");
    }

    public List<Map> queryStatistics(long start, long end) {
        ProjectionOperation projectionOperation = Aggregation.project("unit", "fee", "time");
        Criteria operator = Criteria.where("unit").ne("").andOperator(
                Criteria.where("time").gte(start),
                Criteria.where("time").lt(end)
        );
        MatchOperation matchOperation = Aggregation.match(operator);

        GroupOperation groupOperation = Aggregation.group("unit").sum("fee").as("feeSum");

        // 组合条件
        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);
        // 执行操作
        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "order_detail_aggregation", Map.class);
        List<Map> list = aggregationResults.getMappedResults();
        if (list.size() > 0) {
            list = list.stream().filter(x ->
                    x.get("_id") != null && StringUtils.isNotBlank(x.get("_id").toString())
            ).map(x -> {
                HashMap map = new HashMap(2);
                map.put("name", x.get("_id"));
                map.put("sum", new BigDecimal(x.get("feeSum").toString()).setScale(4, BigDecimal.ROUND_HALF_UP));
                return map;
            }).collect(Collectors.toList());
        }
        return list;
    }

    public List<OrderDetailAggregation> queryStatisticsByUnit(long start, long end,String unit) {
        List<OrderDetailAggregation> list=orderDetailAggregationRepository.findAllByTimeGreaterThanEqualAndTimeLessThanAndUnit(start, end, unit);
        return list;
    }


}
