package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.AdvertiseType;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.OrderDao;
import com.spark.bitrade.dto.OtcApiOrderDto;
import com.spark.bitrade.entity.Country;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.OtcApiAppeal;
import com.spark.bitrade.entity.QOrder;
import com.spark.bitrade.mapper.dao.OtcOrderMapper;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.MyOrderVO;
import com.spark.bitrade.vo.OtcOrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.spark.bitrade.util.IdWorkByTwitter;

/***
  * 
  * @author yangch
  * @time 2018.11.29 10:40
  */

@Service
public class OtcOrderService extends BaseService {
    @Autowired
    private OrderDao orderDao;


//    @Autowired
//    private IdWorkByTwitter idWorkByTwitter;
//    @Autowired
//    private AdvertiseService advertiseService;
//    @Autowired
//    private MemberWalletService memberWalletService;

    @Autowired
    private OtcOrderMapper otcOrderMapper;

    @Autowired
    private LocaleMessageSourceService msService;
    //    @Autowired
//    private MemberTransactionService memberTransactionService;
    @Autowired
    private CountryService countryService;

    public Order findOne(Long id) {
        return orderDao.findOne(id);
    }

    public Order findOneByOrderSn(String orderSn) {
        return orderDao.getOrderByOrderSn(orderSn);
    }

    public int updateOrderAppeal(String orderSn) {
        return orderDao.updateAppealOrder(OrderStatus.APPEAL, orderSn);
    }

    /**
     * 取消订单
     *
     * @param orderSn
     * @return
     */
    public int cancelOrder(String orderSn) {
        return orderDao.cancelOrder(new Date(), OrderStatus.CANCELLED, orderSn);
    }

    //手动订单取消
    public int cancelOrderByhandle(String orderSn, long cancleUserId) {
        return orderDao.cancelOrderByhandle(new Date(), OrderStatus.CANCELLED, orderSn, BooleanEnum.IS_TRUE, cancleUserId);
    }

    /**
     * 订单放行
     *
     * @param orderSn
     * @return
     */
    public int releaseOrder(String orderSn) {
        return orderDao.releaseOrder(new Date(), OrderStatus.COMPLETED, orderSn);
    }

    //edit by tansitao 时间： 2018/5/14 原因：判断用户是否可以进行改订单交易
    @ReadDataSource
    public boolean isAllowTrade(long customerId, int limitNum) {
        boolean isAllowTrade = false;
        int unFinishNum = otcOrderMapper.findUnFinishNum(customerId);
        if (unFinishNum < limitNum) {
            isAllowTrade = true;
        }
        return isAllowTrade;
    }

    public Order findOneByOrderId(String orderId) {
        return orderDao.getOrderByOrderSn(orderId);
    }

    public List<Order> getAllOrdering(Long id) {
        return orderDao.fingAllProcessingOrder(id, OrderStatus.APPEAL, OrderStatus.PAID, OrderStatus.NONPAYMENT);
    }

    public List<Order> findAll() {
        return orderDao.findAll();
    }

    public MessageResult getOrderNum() {
        Predicate predicate = QOrder.order.status.eq(OrderStatus.NONPAYMENT);
        Long noPayNum = orderDao.count(predicate);
        Long paidNum = orderDao.count(QOrder.order.status.eq(OrderStatus.PAID));
        Long finishedNum = orderDao.count(QOrder.order.status.eq(OrderStatus.COMPLETED));
        Long cancelNum = orderDao.count(QOrder.order.status.eq(OrderStatus.CANCELLED));
        Long appealNum = orderDao.count(QOrder.order.status.eq(OrderStatus.APPEAL));
        Map<String, Long> map = new HashMap<>();
        map.put("noPayNum", noPayNum);
        map.put("paidNum", paidNum);
        map.put("finishedNum", finishedNum);
        map.put("cancelNum", cancelNum);
        map.put("appealNum", appealNum);
        return MessageResult.getSuccessInstance("获取成功", map);
    }

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Order> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<Order> list;
        JPAQuery<Order> jpaQuery = queryFactory.selectFrom(QOrder.order);
        if (predicateList != null)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 获取当前会员所有OTC订单
     *
     * @param id       会员信息
     * @param status   状态（0：已取消；1：未付款；2：已付款；3：已完成；4：申诉中；5：已关闭）
     * @param pageNo   页数
     * @param pageSize 条数
     * @param money    金额（￥）
     * @return 当前会员所有OTC订单
     * @author zhongxiaoj
     * @date 2019.08.02
     * @desc 在原有基础上，增加根据金额精确匹配
     */
    public Page<Order> pageQuery(int pageNo, Integer pageSize, OrderStatus status, long id, String orderSn, String unit, AdvertiseType type, BigDecimal money) {
        Sort orders = Criteria.sortStatic("id.desc");
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        Criteria<Order> specification = new Criteria<Order>();
        specification.add(Restrictions.or(Restrictions.eq("memberId", id, false), Restrictions.eq("customerId", id, false)));
        specification.add(Restrictions.eq("status", status, false));
        specification.add(Restrictions.eq("coin.unit", unit, false));
        specification.add(Restrictions.eq("advertiseType", type, false));
        specification.add(Restrictions.ne("orderSourceType", "88888888", false));
        if (StringUtils.isNotBlank(orderSn)) {
            specification.add(Restrictions.like("orderSn", orderSn, false));
        }
        if (money != null) {
            specification.add(Restrictions.eq("money", money, false));
        }
        return orderDao.findAll(specification, pageRequest);
    }

    /**
     *  * 获取所有进行中的订单
     *  * @author tansitao
     *  * @time 2018/12/26 17:01 
     *  
     */
    public List<Order> getAllGoingOrder(long memberId) {
        Criteria<Order> specification = new Criteria<Order>();
        Criteria.sortStatic("id.desc");
        specification.add(Restrictions.or(Restrictions.eq("memberId", memberId, false), Restrictions.eq("customerId", memberId, false)));
        specification.add(Restrictions.or(Restrictions.eq("status", OrderStatus.NONPAYMENT, false),
                Restrictions.eq("status", OrderStatus.PAID, false),
                Restrictions.eq("status", OrderStatus.APPEAL, false)));
        specification.add(Restrictions.ne("orderSourceType", "88888888", false));
        return orderDao.findAll(specification);
    }

    /**
     * 超时订单
     *
     * @return
     */
    public List<String> checkExpiredOrder() {
        return otcOrderMapper.findAllExpiredOrderSn(new Date());
    }

    public Page<Order> findAll(com.querydsl.core.types.Predicate predicate, Pageable pageable) {
        return orderDao.findAll(predicate, pageable);
    }

    /**
     * 分页查询
     *
     * @param predicates
     * @param pageModel
     * @return true
     * @author shenzucai
     * @time 2018.06.13 10:26
     */
    public Page<OtcOrderVO> joinFind(List<Predicate> predicates, PageModel pageModel) {
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        JPAQuery<OtcOrderVO> query = queryFactory.select(
                Projections.fields(OtcOrderVO.class,
                        QOrder.order.id.as("id"),
                        QOrder.order.advertiseId.as("advertiseId"),//add by zyj 2018/8/28 增加广告id
                        QOrder.order.orderSn.as("orderSn"),
                        QOrder.order.advertiseType.as("advertiseType"),
                        QOrder.order.createTime.as("createTime"),
                        QOrder.order.memberName.as("memberName"),
                        QOrder.order.customerId.as("customerId"),
                        QOrder.order.customerName.as("customerName"),
                        QOrder.order.coin.unit,
                        QOrder.order.money,
                        QOrder.order.number,
                        QOrder.order.commission.as("fee"),
                        QOrder.order.payMode.as("payMode"),
                        QOrder.order.releaseTime.as("releaseTime"),
                        QOrder.order.cancelTime.as("cancelTime"),
                        QOrder.order.payTime.as("payTime"),
                        QOrder.order.price,//add by tansitao 时间： 2018/6/30 原因：增加成交价格
                        QOrder.order.status.as("status"))
        ).from(QOrder.order).where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<OtcOrderVO> list = query.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(list, pageModel.getPageable(), total);
    }

    /**
     * excel导出
     *
     * @param predicates
     * @param pageModel
     * @return true
     * @author shenzucai
     * @time 2018.06.13 10:26
     */
    public List<OtcOrderVO> outExcel(List<Predicate> predicates, PageModel pageModel) {
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        JPAQuery<OtcOrderVO> query = queryFactory.select(
                Projections.fields(OtcOrderVO.class,
                        QOrder.order.id.as("id"),
                        QOrder.order.orderSn.as("orderSn"),
                        QOrder.order.advertiseType.as("advertiseType"),
                        QOrder.order.createTime.as("createTime"),
                        QOrder.order.memberName.as("memberName"),
                        QOrder.order.customerId.as("customerId"),
                        QOrder.order.customerName.as("customerName"),
                        QOrder.order.coin.unit,
                        QOrder.order.money,
                        QOrder.order.number,
                        QOrder.order.commission.as("fee"),
                        QOrder.order.payMode.as("payMode"),
                        QOrder.order.releaseTime.as("releaseTime"),
                        QOrder.order.cancelTime.as("cancelTime"),
                        QOrder.order.payTime.as("payTime"),
                        QOrder.order.status.as("status"),
                        QOrder.order.price.as("price"))
        ).from(QOrder.order).where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        // 单词最多导出60000条记录 todo 后续支持更多
        query.limit(60000);
        List<OtcOrderVO> list = query.fetch();
        //edit by zyj
        for (int i = 0; i < list.size(); i++) {
            BigDecimal orderSn = new BigDecimal(list.get(i).getOrderSn());
            list.get(i).setOrderSnOut(orderSn);//订单编号String转为BigDecimal
            list.get(i).setAdvertiseTypeOut(list.get(i).getAdvertiseType().getCnName());//广告类型
            list.get(i).setOrderStatusOut(list.get(i).getStatus().getCnName());//订单状态
//            list.get(i).setCreateTimeOut(DateUtil.dateToString(==null?null:list.get(i).getCreateTime()));//交易时间
            Date createTime = list.get(i).getCreateTime();
            if (createTime != null)
                list.get(i).setCreateTimeOut(DateUtil.dateToString(createTime, "yyyy-MM-dd HH:mm:ss"));
            //订单取消时间
            Date cancelTime1 = list.get(i).getCancelTime();
            if (cancelTime1 != null)
                list.get(i).setCancelTimeOut(DateUtil.dateToString(cancelTime1, "yyyy-MM-dd HH:mm:ss"));
            //放行时间
            Date releaseTime = list.get(i).getReleaseTime();
            if (releaseTime != null)
                list.get(i).setReleaseTimeOut(DateUtil.dateToString(releaseTime, "yyyy-MM-dd HH:mm:ss"));
            //订单支付时间
            Date payTime = list.get(i).getPayTime();
            if (payTime != null)
                list.get(i).setPayTimeOut(DateUtil.dateToString(payTime, "yyyy-MM-dd HH:mm:ss"));


        }
        return list;
    }

    public List<Map<String, Long>> selectCountByMembers(Long[] memberIds, AdvertiseType type) {
        return otcOrderMapper.findCountByMembers(memberIds, type == AdvertiseType.BUY ? 0 : 1);
    }

    public List<Map<String, Long>> selectCountByMembersAnd48(Long[] memberIds, AdvertiseType type, Date date) {
        return otcOrderMapper.selectCountByMembersAnd48(memberIds, type == AdvertiseType.BUY ? 0 : 1, date);
    }


    public boolean updateReleaseTime(String orderSn) {
        int row = orderDao.updateReleaseTime(new Date(), orderSn);
        return row > 0 ? true : false;
    }


    /**
     * 根据条件查询订单
     *
     * @param pageNo
     * @param pageSize
     * @param memberId
     * @param status
     * @param orderSn
     * @param unit
     * @param type     交易类型 0买 1卖
     * @param money    金额，精确匹配
     * @return
     */
    @ReadDataSource
    public PageInfo<MyOrderVO> findOrderBy(int pageNo, int pageSize, long memberId, int status, String orderSn, String unit, int type, BigDecimal money) {
        com.github.pagehelper.Page<MyOrderVO> page = PageHelper.startPage(pageNo, pageSize);
        this.getOrderBy(memberId, status, orderSn, unit, type, money);
        return page.toPageInfo();
    }

    List<MyOrderVO> getOrderBy(long memberId, int status, String orderSn, String unit, int type, BigDecimal money) {
        List<MyOrderVO> list = otcOrderMapper.findOrderBy(memberId, status, orderSn, unit, type, money);
//        for (int i = 0; i < list.size(); i++) {
//            Country country = countryService.findOne(list.get(i).getCountryName());
//            list.get(i).setCountry(country);
//        }
        return list;
    }

    /**
     * 查询流水记录（注意：仅为客户的流水记录）
     *
     * @param memberId  用户ID
     * @param appId     应用ID/渠道ID
     * @param pageNo
     * @param pageSize
     * @param startTime 开始时间，可选
     * @param endTime   截至时间，可选
     * @return
     */
    @ReadDataSource
    public PageInfo<OtcOrderVO> findRecordByUidAndAppId(Long coinId, Long memberId, Long appId,
                                                        Integer pageNo, Integer pageSize,
                                                        String startTime, String endTime) {
        com.github.pagehelper.Page<OtcOrderVO> page = PageHelper.startPage(pageNo, pageSize);
        //PageHelper会自动拦截到下面这查询sql
        this.otcOrderMapper.findRecordByUidAndAppId(coinId, memberId, appId, startTime, endTime);
        return page.toPageInfo();
    }

    /**
     * 获取指定时间就过期的订单
     *
     * @param times 时间节点
     * @return 即将过期的订单编号
     */
    public List<String> expireRemindOrder(Long times) {
        return otcOrderMapper.findAllExpireRemindOrder(times);
    }


    /**
     * 查询otcApi订单
     *
     * @param orderSn
     * @return
     */
    public OtcApiOrderDto findOtcApiOrderByorderSn(String orderSn) {
        return otcOrderMapper.findOtcApiOrderByorderSn(orderSn);
    }

    /**
     * 查询otcApi订单
     *
     * @param OrderId
     * @return
     */
    public OtcApiOrderDto findOtcApiOrderByOrderId(Long OrderId) {
        return otcOrderMapper.findOtcApiOrderByorderId(OrderId);
    }

    /**
     * 更新otcapiorder
     *
     * @param status
     * @param orderId
     * @param oldStatus
     * @return
     */
    public int updateOtcApiOrder(int status, Long orderId, int oldStatus) {
        return orderDao.updateOtcApiOrder(status, orderId, oldStatus);
    }

    /**
     * 更新老的otcOrder
     *
     * @param status
     * @param orderId
     * @param oldStatus
     * @return
     */
    public int updateOldOtcOrder(int status, Long orderId, int oldStatus) {
        return orderDao.updateOldOtcOrder(status, orderId, oldStatus);
    }

    /**
     * 查询appeal根据otcApiOrder
     *
     * @param orderId
     * @return
     */
    public OtcApiAppeal findAppealByOtcApiOrder(Long orderId) {
        return otcOrderMapper.findAppealByOtcApiOrder(orderId);
    }

}
