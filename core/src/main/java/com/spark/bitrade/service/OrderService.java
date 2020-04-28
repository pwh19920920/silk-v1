package com.spark.bitrade.service;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.dao.MemberWalletDao;
import com.spark.bitrade.dao.OrderDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.mapper.dao.OtcOrderMapper;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.OtcOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.util.BigDecimalUtils.add;

/**
 * 该类已计划废弃，使用OtcOrderService类，没有迁移的方法会进一步迁移走
 *
 * @author Zhang Jinwei
 * @date 2017年12月11日
 */
@Deprecated
@Service
@Slf4j
public class OrderService extends BaseService {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private IdWorkByTwitter idWorkByTwitter;
    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private OtcOrderMapper otcOrderMapper;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private OtcOrderService otcOrderService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletDao memberWalletDao;

    /*@Transactional(rollbackFor = Exception.class)
    public void cancelOrderTask(Order order) throws InformationExpiredException {
        if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
            //更改钱包
            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            MessageResult result = memberWalletService.thawBalance(memberWallet, order.getNumber());
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
            //更改钱包
            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            MessageResult result = memberWalletService.thawBalance(memberWallet, add(order.getNumber(), order.getCommission()));
            if (result.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        }
        //取消订单
        if (!(this.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
    }*/
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderTask(String orderSn) throws UnexpectedException {
        Order order = findOneByOrderSn(orderSn);
        if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new UnexpectedException("更改广告失败，订单号为：" + orderSn);
            }
            //更改钱包
            //MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            MemberWallet memberWallet = memberWalletService.findCacheByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            MessageResult result = memberWalletService.thawBalance(memberWallet, order.getNumber());
            if (result.getCode() != 0) {
                throw new UnexpectedException("解冻钱包余额失败，订单号为：" + orderSn);
            }
        } else {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new UnexpectedException("更改广告失败，订单号为：" + orderSn);
            }
            //更改钱包
            //MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            MemberWallet memberWallet = memberWalletService.findCacheByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            MessageResult result = memberWalletService.thawBalance(memberWallet, add(order.getNumber(), order.getCommission()));
            if (result.getCode() != 0) {
                throw new UnexpectedException("解冻钱包余额失败，订单号为：" + orderSn);
            }
        }
        //取消订单
        if (!(this.cancelOrder(order.getOrderSn()) > 0)) {
            throw new UnexpectedException("撤单失败，订单号为：" + orderSn);
        }
    }

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Deprecated
    public PageResult<Order> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        return otcOrderService.query(predicateList, pageNo, pageSize);
    }
    /*@Transactional(readOnly = true)
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
    }*/

    @Deprecated
    public Order findOne(Long id) {
        return otcOrderService.findOne(id);
        //return orderDao.findOne(id);
    }

    @Deprecated
    public Order findOneByOrderSn(String orderSn) {
        return otcOrderService.findOneByOrderSn(orderSn);
        //return orderDao.getOrderByOrderSn(orderSn);
    }

    @Deprecated
    public int updateOrderAppeal(String orderSn) {
        return otcOrderService.updateOrderAppeal(orderSn);
        //return orderDao.updateAppealOrder(OrderStatus.APPEAL, orderSn);
    }

    @Transactional(rollbackFor = Exception.class)
    public int payForOrder(String orderSn, PayMode payMode, String payModeInfo) {
        return orderDao.updatePayOrder(new Date(), OrderStatus.PAID, orderSn, payMode, payModeInfo); //add by tansitao 时间： 2018/9/6 原因：增加支付方式
    }

    @Transactional(rollbackFor = Exception.class)
    public int payForOrderBOA(String orderSn, PayMode payMode, String payModeInfo) {
        int i = orderDao.updatePayOrder(new Date(), OrderStatus.PAID, orderSn, payMode, payModeInfo); //add by tansitao 时间： 2018/9/6 原因：增加支付方式
        if (i > 0) {
            i = orderDao.updatePayOrderBOA(new Date(), OrderStatus.PAID.getOrdinal(), orderSn, payMode.getOrdinal(), payModeInfo);
        }
        return i;
    }

    /**
     * 取消订单
     *
     * @param orderSn
     * @return
     */
    @Deprecated
    public int cancelOrder(String orderSn) {
        return otcOrderService.cancelOrder(orderSn);
        //return orderDao.cancelOrder(new Date(), OrderStatus.CANCELLED, orderSn);
    }

    /**
     * 关闭订单
     *
     * @param orderSn
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int closeOrder(String orderSn) {
        return orderDao.closeOrder(new Date(), OrderStatus.CLOSE, orderSn);
    }

    //add by tansitao 时间： 2018/4/25 原因：手动订单取消
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByhandle(Order order, long cancleUserId, int ret) throws UnexpectedException {
        if (!(otcOrderService.cancelOrderByhandle(order.getOrderSn(), cancleUserId) > 0)) {
            throw new UnexpectedException(String.format("OTCQX001：%s", msService.getMessage("CANCEL_FAILED")));
        }

        MemberWallet memberWallet;
        MessageResult result;
        if (ret == 1) {
            //更改广告
            //创建订单的时候减少了realAmount，增加了dealAmount，撤销时只减少了dealAmount的金额，没有增加realAmount的金额
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new UnexpectedException(String.format("OTCQX002：", msService.getMessage("CANCEL_FAILED")));
            }
            memberWallet = memberWalletService.findCacheByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            result = memberWalletService.thawBalance(memberWallet, order.getNumber());
            if (result.getCode() != 0) {
                throw new UnexpectedException(String.format("OTCQX003：%s", msService.getMessage("CANCEL_FAILED")));
            }
        } else {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new UnexpectedException(String.format("OTCQX004：%s", msService.getMessage("CANCEL_FAILED")));
            }
            memberWallet = memberWalletService.findCacheByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            result = memberWalletService.thawBalance(memberWallet, add(order.getNumber(), order.getCommission()));
            if (result.getCode() != 0) {
                throw new UnexpectedException(String.format("OTCQX005：%s", msService.getMessage("CANCEL_FAILED")));
            }
        }
    }


    /**
     * 处理btbank otc api 过来的订单
     *
     * @param order
     * @param cancleUserId
     * @param ret
     * @return true
     * @author shenzucai
     * @time 2019.10.01 14:47
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByhandlecancelBOA(Order order, long cancleUserId, int ret) throws UnexpectedException {
        if (!(otcOrderService.cancelOrderByhandle(order.getOrderSn(), cancleUserId) > 0)) {
            throw new UnexpectedException(String.format("OTCQX001：%s", msService.getMessage("CANCEL_FAILED")));
        }

        if (ret == 1) {
            //更改广告
            //创建订单的时候减少了realAmount，增加了dealAmount，撤销时只减少了dealAmount的金额，没有增加realAmount的金额
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new UnexpectedException(String.format("OTCQX002：", msService.getMessage("CANCEL_FAILED")));
            }
            // 更改otc_api_order 状态
            int update = orderDao.cancelOrderByhandleBOA(new Date(), OrderStatus.CANCELLED.getOrdinal(), order.getOrderSn(), cancleUserId);
            if (update <= 0) {
                throw new UnexpectedException(String.format("OTCQX003：%s", msService.getMessage("CANCEL_FAILED")));
            }
            // 退还api用户的冻结
            int upadteBalance = orderDao.cancelOrderBalanceByhandleBOA(new Date(), order.getOrderMoney(), order.getOrderSn(), order.getCustomerId());
            if (upadteBalance <= 0) {
                throw new UnexpectedException(String.format("OTCQX003：%s", msService.getMessage("CANCEL_FAILED")));
            }

        }

    }

    /**
     * 订单放行
     *
     * @param orderSn
     * @return
     */
    @Deprecated
    public int releaseOrder(String orderSn) {
        return otcOrderService.releaseOrder(orderSn);
        //return orderDao.releaseOrder(new Date(), OrderStatus.COMPLETED, orderSn);
    }

    /**
     * 生成订单
     *
     * @param order
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Order saveOrder(Order order) {
        order.setOrderSn(String.valueOf(idWorkByTwitter.nextId()));
        return orderDao.saveAndFlush(order);
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
    @Deprecated
    public Page<Order> pageQuery(int pageNo, Integer pageSize, OrderStatus status, long id, String orderSn, String unit, AdvertiseType type, BigDecimal money) {
        return otcOrderService.pageQuery(pageNo, pageSize, status, id, orderSn, unit, type, money);
    }

    public Map getOrderBySn(Long memberId, String orderSn) {
        String sql = "select o.*,m.real_name from otc_order o  join member m on o.customer_id=m.id and o.member_id=:memberId and o.order_sn =:orderSn ";
        Query query = em.createNativeQuery(sql);
        //设置结果转成Map类型
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        Object object = query.setParameter("memberId", memberId).setParameter("orderSn", orderSn).getSingleResult();
        Map map = (HashMap) object;
        return map;
    }

    //edit by yangch 时间： 2018.07.27 原因：接口优化，从只读库中获取
    /*public List<Order> checkExpiredOrder() {
        return orderDao.findAllExpiredOrder(new Date());
    }*/
    @Deprecated
    public List<String> checkExpiredOrder() {
        return otcOrderService.checkExpiredOrder();
        //return otcOrderMapper.findAllExpiredOrderSn(new Date());
    }

    //edit by tansitao 时间： 2018/5/14 原因：判断用户是否可以进行改订单交易
    //@ReadDataSource
    @Deprecated
    public boolean isAllowTrade(long customerId, int limitNum) {
        return otcOrderService.isAllowTrade(customerId, limitNum);

        /*boolean isAllowTrade = false;
        int unFinishNum = otcOrderMapper.findUnFinishNum(customerId);
        if(unFinishNum < limitNum)
        {
            isAllowTrade = true;
        }
        return isAllowTrade;*/
    }

    /**
     *  * 通过取消订单数判断是否允许用户继续交易
     *  * @author tansitao
     *  * @time 2018/6/30 16:35 
     *  
     */

    @ReadDataSource
    public boolean isAllowTradeByCancelNum(long customerId, int orderCancleNum) {
        boolean isAllowTrade = false;
        int cancelNum = otcOrderMapper.findTodayAllCancel(customerId, DateUtil.dateToStringDate(new Date()));
        if (cancelNum < orderCancleNum) {
            isAllowTrade = true;
        }
        return isAllowTrade;
    }

    @Deprecated
    public List<Order> findAll() {
        return otcOrderService.findAll();
        //return orderDao.findAll();
    }

    public Order save(Order order) {
        return orderDao.save(order);
    }

    @Deprecated
    public MessageResult getOrderNum() {
        return otcOrderService.getOrderNum();
        /*Predicate predicate = QOrder.order.status.eq(OrderStatus.NONPAYMENT);
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
        return MessageResult.getSuccessInstance("获取成功", map);*/
    }

    @Deprecated
    public List<Order> getAllOrdering(Long id) {
        return otcOrderService.getAllOrdering(id);

        //return orderDao.fingAllProcessingOrder(id, OrderStatus.APPEAL, OrderStatus.PAID, OrderStatus.NONPAYMENT);
    }

    @Deprecated
    public Order findOneByOrderId(String orderId) {
        return otcOrderService.findOneByOrderId(orderId);
        //return orderDao.getOrderByOrderSn(orderId);
    }

    @Deprecated
    public Page<Order> findAll(com.querydsl.core.types.Predicate predicate, Pageable pageable) {
        return otcOrderService.findAll(predicate, pageable);
        //return orderDao.findAll(predicate, pageable);
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
    @Deprecated
    public Page<OtcOrderVO> joinFind(List<Predicate> predicates, PageModel pageModel) {
        return otcOrderService.joinFind(predicates, pageModel);
        /*List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
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
                        QOrder.order.isManualCancel.as("isManualCancel"),//add by zyj 2018.11.29 增加是否手动取消订单
                        QOrder.order.price,//add by tansitao 时间： 2018/6/30 原因：增加成交价格
                        QOrder.order.status.as("status"))
        ).from(QOrder.order).where(predicates.toArray(new BooleanExpression[predicates.size()]));
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<OtcOrderVO> list = query.offset((pageModel.getPageNo()-1)*pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount() ;
        return new PageImpl<>(list,pageModel.getPageable(),total);*/
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
        return otcOrderService.outExcel(predicates, pageModel);
        /*List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
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
        for (int i=0;i<list.size();i++){
            BigDecimal orderSn=new BigDecimal(list.get(i).getOrderSn());
            list.get(i).setOrderSnOut(orderSn);//订单编号String转为BigDecimal
            list.get(i).setAdvertiseTypeOut(list.get(i).getAdvertiseType().getCnName());//广告类型
            list.get(i).setOrderStatusOut(list.get(i).getStatus().getCnName());//订单状态
//            list.get(i).setCreateTimeOut(DateUtil.dateToString(==null?null:list.get(i).getCreateTime()));//交易时间
            Date createTime=list.get(i).getCreateTime();
            if (createTime!=null)
                list.get(i).setCreateTimeOut(DateUtil.dateToString(createTime,"yyyy-MM-dd HH:mm:ss"));
            //订单取消时间
            Date cancelTime1=list.get(i).getCancelTime();
            if (cancelTime1!=null)
                list.get(i).setCancelTimeOut(DateUtil.dateToString(cancelTime1,"yyyy-MM-dd HH:mm:ss"));
            //放行时间
            Date releaseTime=list.get(i).getReleaseTime();
            if (releaseTime!=null)
                list.get(i).setReleaseTimeOut(DateUtil.dateToString(releaseTime,"yyyy-MM-dd HH:mm:ss"));
            //订单支付时间
            Date payTime=list.get(i).getPayTime();
            if (payTime!=null)
                list.get(i).setPayTimeOut(DateUtil.dateToString(payTime,"yyyy-MM-dd HH:mm:ss"));


        }
        return list;*/
    }


    /**
     * c2c买币
     *
     * @param order     订单信息
     * @param advertise 广告信息
     * @param amount    购买数量
     * @return
     * @throws UnexpectedException
     */
    @Transactional(rollbackFor = Exception.class)
    public Order buyOrder(Order order, final Advertise advertise, final BigDecimal amount) throws UnexpectedException {
        order.setOrderSn(String.valueOf(idWorkByTwitter.nextId()));
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new UnexpectedException(String.format("OTCGM001：%s", msService.getMessage("CREATE_ORDER_FAILED")));
        }

        return orderDao.saveAndFlush(order);
    }

    /**
     * c2c卖币
     *
     * @param order     订单信息
     * @param advertise 广告信息
     * @param wallet    冻结钱包
     * @param amount    卖币数量
     * @return
     * @throws UnexpectedException
     */
    @Transactional(rollbackFor = Exception.class)
    public Order sellOrder(Order order, final Advertise advertise, final MemberWallet wallet, final BigDecimal amount) throws UnexpectedException {
        order.setOrderSn(String.valueOf(idWorkByTwitter.nextId()));
        Order orderNew = orderDao.saveAndFlush(order);

        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new UnexpectedException(String.format("OTCMB001：%s", msService.getMessage("SELL_FAILED")));
        }

        //注：账号表 是瓶颈，放到最后尽可能的减少事务锁的等待时间
        if (!(memberWalletService.freezeBalance(wallet, amount).getCode() == 0)) {
            throw new UnexpectedException(String.format("OTCMB002：%s", msService.getMessage("SELL_FAILED")));
        }

        return orderNew;
    }

    /**
     * 订单放行
     *
     * @param order 订单信息
     * @param ret   类型
     * @param user  授权用户
     * @throws UnexpectedException
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(final Order order, final int ret, final AuthMember user) throws UnexpectedException {

        if (ret == 1) {
            //用户是放行者
            MemberTransaction memberTransaction = new MemberTransaction();
            if("USDC".equals(order.getCoin().getUnit())){
                //USDC 永远只扣除用户手续费
                memberTransaction.setAmount(order.getNumber());
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setFee(order.getCommission());
            }else{
                memberTransaction.setAmount(order.getNumber());
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setFee(BigDecimal.ZERO);
            }
            memberTransaction.setSymbol(order.getCoin().getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL);
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setRefId(order.getOrderSn());
            memberTransactionService.save(memberTransaction);

            MemberTransaction memberTransaction1 = new MemberTransaction();
            if("USDC".equals(order.getCoin().getUnit())){
                //USDC 永远只扣除用户手续费
                memberTransaction1.setAmount(order.getNumber().subtract(order.getCommission()));
                memberTransaction1.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction1.setFee(BigDecimal.ZERO);
            }else{
                memberTransaction1.setAmount(order.getNumber());
                memberTransaction1.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction1.setFee(order.getCommission());
            }
            memberTransaction1.setType(TransactionType.OTC_BUY);
            memberTransaction1.setMemberId(order.getMemberId());
            memberTransaction1.setSymbol(order.getCoin().getUnit());
            memberTransaction1.setRefId(order.getOrderSn());
            memberTransactionService.save(memberTransaction1);

            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new UnexpectedException(String.format("OTCFX001：%s", msService.getMessage("RELEASE_FAILED")));
            }
        } else {
            //商家是放行者
            MemberTransaction memberTransaction = new MemberTransaction();
            if("USDC".equals(order.getCoin().getUnit())){
                //USDC 永远只扣除用户手续费
                memberTransaction.setAmount(order.getNumber());
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setFee(BigDecimal.ZERO);
            }else{
                memberTransaction.setAmount(order.getNumber());
                memberTransaction.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction.setFee(order.getCommission());
            }
            memberTransaction.setSymbol(order.getCoin().getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL);
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setRefId(order.getOrderSn());
            memberTransactionService.save(memberTransaction);

            MemberTransaction memberTransaction1 = new MemberTransaction();
            if("USDC".equals(order.getCoin().getUnit())){
                //USDC 永远只扣除用户手续费
                memberTransaction1.setAmount(order.getNumber());
                memberTransaction1.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction1.setFee(order.getCommission());
            }else{
                memberTransaction1.setAmount(order.getNumber());
                memberTransaction1.setFeeDiscount(BigDecimal.ZERO);
                memberTransaction1.setFee(BigDecimal.ZERO);
            }
            memberTransaction1.setType(TransactionType.OTC_BUY);
            memberTransaction1.setMemberId(order.getCustomerId());
            memberTransaction1.setSymbol(order.getCoin().getUnit());
            memberTransaction1.setRefId(order.getOrderSn());
            memberTransactionService.save(memberTransaction1);

            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new UnexpectedException(String.format("OTCFX002：%s", msService.getMessage("RELEASE_FAILED")));
            }
        }

        //放行订单
        if (!(releaseOrder(order.getOrderSn()) > 0)) {
            throw new UnexpectedException(String.format("OTCFX003：%s", msService.getMessage("RELEASE_FAILED")));
        }

        //更改钱包
        MessageResult result = memberWalletService.transfer(order, ret);
        if (result.getCode() == -1) {
            throw new UnexpectedException(String.format("OTCFX004：%s", msService.getMessage("RELEASE_FAILED")));
        } else if (result.getCode() == -2) {
            throw new UnexpectedException(String.format("OTCFX005：%s", msService.getMessage("RELEASE_FAILED")));
        }
        try {
            // 20190826 增加手续费归集
            getService().saveOtcSummarizeRecord(order.getOrderSn(), order.getCommission(), order.getCoin());
        } catch (Exception e) {
            log.error("============增加归集报错========");
        }

    }

    /**
     * 订单放行
     *
     * @param order
     * @param ret
     * @param memberId
     * @return true
     * @author shenzucai
     * @time 2019.10.01 18:45
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrderBOA(final Order order, final int ret, final Long memberId) throws UnexpectedException {

        if (ret == 1) {

            MemberTransaction memberTransaction1 = new MemberTransaction();
            memberTransaction1.setAmount(BigDecimalUtils.sub(order.getOrderMoney(), order.getCommission()));
            memberTransaction1.setType(TransactionType.OTC_BUY);
            memberTransaction1.setMemberId(order.getMemberId());
            memberTransaction1.setSymbol(order.getCoin().getUnit());
            memberTransaction1.setFee(order.getCommission());
            memberTransaction1.setRefId(order.getOrderSn());
            memberTransaction1.setFeeDiscount(BigDecimal.ZERO);
            memberTransactionService.save(memberTransaction1);

            // api用户资金流水
            orderDao.saveOtcApiTransaction(idWorkByTwitter.nextId(), order.getCustomerId(), order.getOrderMoney(), 1, order.getCommission(), order.getOrderSn());
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new UnexpectedException(String.format("OTCFX001：%s", msService.getMessage("RELEASE_FAILED")));
            }
        }

        //放行订单
        if (!(releaseOrder(order.getOrderSn()) > 0)) {
            throw new UnexpectedException(String.format("OTCFX003：%s", msService.getMessage("RELEASE_FAILED")));
        }

        if (orderDao.releaseOrderBOA(new Date(), OrderStatus.COMPLETED.getOrdinal(), order.getOrderSn()) < 1) {
            throw new UnexpectedException(String.format("OTCFX003：%s", msService.getMessage("RELEASE_FAILED")));
        }

        //更改钱包
        MessageResult result = memberWalletService.transferBOA(order, ret);
        if (result.getCode() == -1) {
            throw new UnexpectedException(String.format("OTCFX004：%s", msService.getMessage("RELEASE_FAILED")));
        } else if (result.getCode() == -2) {
            throw new UnexpectedException(String.format("OTCFX005：%s", msService.getMessage("RELEASE_FAILED")));
        }
        try {
            // 20190826 增加手续费归集
            getService().saveOtcSummarizeRecord(order.getOrderSn(), order.getCommission(), order.getCoin());
        } catch (Exception e) {
            log.error("============增加归集报错========");
        }

    }

    /**
     * 手续费归集
     *
     * @param orderSn    订单号
     * @param commission 手续费
     * @param coin       币种
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult saveOtcSummarizeRecord(String orderSn, BigDecimal commission, OtcCoin coin) {
        // 判断手续费是否为0
        if (commission.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.success("手续费为0，无需归集");
        }
        Long summarizeMemberId;
        //add by ss 时间：2020/03/29 原因：USDC归集到 会员费归集/返佣账号
        if("USDC".equals(coin.getUnit())){
            summarizeMemberId = otcCoinService.getOtcSummarizeAccountOfUSDC();
        }else{
            summarizeMemberId = otcCoinService.getOtcSummarizeAccount();
        }
        // 判断是否存在归集账户
        if (summarizeMemberId == null || summarizeMemberId == 0) {
            return MessageResult.success("未找到归集的账号，方法终止");
        }
        // 归集流水写入
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setMemberId(summarizeMemberId);
        memberTransaction.setRefId(orderSn);
        memberTransaction.setType(TransactionType.OTC_JY_RATE_FEE);
        memberTransaction.setAmount(commission);
        memberTransaction.setSymbol(coin.getUnit());
        memberTransactionService.save(memberTransaction);
        Coin c = coinService.findByUnit(coin.getUnit());
        // 钱包不存在，则先创建钱包
        MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(coin, summarizeMemberId);
        if (memberWallet == null) {
            memberWallet = memberWalletService.createMemberWallet(summarizeMemberId, c);
        }
        int a = memberWalletDao.increaseBalance(memberWallet.getId(), commission);
        Assert.isTrue(a > 0, "法币交易，手续费归集失败");
        return MessageResult.success("法币交易，手续费归集成功");
    }

    /**
     * 获取指定时间就过期的订单
     *
     * @param times 时间节点
     * @return 即将过期的订单编号
     */
    public List<String> expireRemindOrder(Long times) {
        return otcOrderService.expireRemindOrder(times);
    }

    private OrderService getService() {
        return SpringContextUtil.getBean(OrderService.class);
    }


    /**
     * 确认付款
     * @param orderSn 订单号
     * @param payModeId 交易方式ID
     * @param payModeInfo 交易方式内容
     * @param allPayInfo 付款方支付方式快照
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int orderPayMethod(String orderSn, Long payModeId, String payModeInfo,String allPayInfo) {
        return orderDao.orderPayMethod(new Date(), OrderStatus.PAID,orderSn,payModeId,payModeInfo,allPayInfo);
    }
}
