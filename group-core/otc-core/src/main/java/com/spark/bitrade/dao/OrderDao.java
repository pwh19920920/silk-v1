package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.constant.PayMode;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Order;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zhang Jinwei
 * @date 2017年12月11日
 */
public interface OrderDao extends BaseDao<Order> {

    /**
     * 根据用户id和订单编号查询订单
     *
     * @param memberId 用户id
     * @param orderSn  订单编号
     * @return
     */
    List<Order> getOrderByMemberIdAndOrderSn(Long memberId, String orderSn);

    /**
     * 根据用户id查询订单
     *
     * @param memberId 用户id
     * @return
     */
    List<Order> getOrderByMemberId(Long memberId);

    /**
     * 根据用户id和状态查询订单
     *
     * @param memberId 用户id
     * @param status   状态
     * @return
     */
    List<Order> getOrderByMemberIdAndStatus(Long memberId, OrderStatus status);

    /**
     * 根据用户id、订单编号和状态查询订单
     *
     * @param memberId
     * @param status
     * @param orderSn
     * @return
     */
    List<Order> getOrderByMemberIdAndStatusAndOrderSn(
            Long memberId, OrderStatus status, String orderSn);

    List<Order> findByAdvertiseId(Long advertiseId);

    Order getOrderByOrderSn(String v2);

    // edit by tansitao 时间： 2018/9/4 原因：增加对支付方式的修改
    @Modifying
    @Query(
            "update Order a set a.payTime=:date,a.status=:status,a.payMethod=:payMode,a.payMethodInfo=:payModeInfo where a.status=1 and a.orderSn=:orderSn")
    int updatePayOrder(
            @Param("date") Date date,
            @Param("status") OrderStatus status,
            @Param("orderSn") String orderSn,
            @Param("payMode") PayMode payMode,
            @Param("payModeInfo") String payModeInfo);

    @Modifying
    @Query(
            "update Order a set a.cancelTime=:date,a.status=:status where (a.status=1 or a.status=2 or a.status=4) and a.orderSn=:orderSn")
    int cancelOrder(
            @Param("date") Date date,
            @Param("status") OrderStatus status,
            @Param("orderSn") String orderSn);

    // 更新放行时间
    @Modifying
    @Query("update Order a set a.releaseTime=:date where a.orderSn=:orderSn")
    int updateReleaseTime(@Param("date") Date date, @Param("orderSn") String orderSn);

    // 关闭订单
    @Modifying
    @Query(
            "update Order a set a.closeTime=:date,a.status=:status where (a.status=1 or a.status=2 or a.status=4) and a.orderSn=:orderSn")
    int closeOrder(
            @Param("date") Date date,
            @Param("status") OrderStatus status,
            @Param("orderSn") String orderSn);

    // add by tansitao 时间： 2018/4/25 原因：添加手动取消订单
    @Modifying
    @Query(
            "update Order a set a.cancelTime=:date,a.status=:status,a.isManualCancel=:isManualCancel,a.cancelMemberId=:cancelMemberId where (a.status=1 or a.status=2 or a.status=4) and a.orderSn=:orderSn")
    int cancelOrderByhandle(
            @Param("date") Date date,
            @Param("status") OrderStatus status,
            @Param("orderSn") String orderSn,
            @Param("isManualCancel") BooleanEnum isManualCancel,
            @Param("cancelMemberId") long cancelMemberId);

    @Modifying
    @Query(
            "update Order a set a.releaseTime=:date,a.status=:status where  (a.status=2 or a.status=4) and a.orderSn=:orderSn")
    int releaseOrder(
            @Param("date") Date date,
            @Param("status") OrderStatus status,
            @Param("orderSn") String orderSn);

    // add 时间： 2018/5/14 原因：查询未支付和已支付的订单
    @Query("select o from Order o where o.customerId = :customerId and (o.status=1 or o.status=2)")
    List<Order> findAllByStatus(@Param("customerId") long customerId);

    @Query(
            "select a from Order a where timestampdiff(MINUTE,a.createTime,:date)>=a.timeLimit and a.status=1")
    List<Order> findAllExpiredOrder(@Param("date") Date date);

    @Query(
            "select a from Order a where (a.memberId=:myId or a.customerId=:myId) and (a.status=:unPay or a.status=:paid or a.status=:appeal)")
    List<Order> fingAllProcessingOrder(
            @Param("myId") Long myId,
            @Param("unPay") OrderStatus unPay,
            @Param("paid") OrderStatus paid,
            @Param("appeal") OrderStatus appeal);

    @Modifying
    @Query("update Order a set a.status=:status where a.status=2 and a.orderSn=:orderSn")
    int updateAppealOrder(@Param("status") OrderStatus status, @Param("orderSn") String orderSn);

    int countByCreateTimeBetween(Date startTime, Date endTime);

    int countByStatusAndCreateTimeBetween(OrderStatus status, Date startTime, Date endTime);

    int countByStatus(OrderStatus status);

    // add by tansitao 时间： 2018/6/12 原因：合并保证金功能
    @Query(
            value =
                    "select a.unit unit,date_format(b.release_time,'%Y-%m-%d'), sum(b.number) amount ,sum(b.commission) fee ,sum(money) from otc_order b ,otc_coin a where a.id = b.coin_id and b.status = 3 and date_format(b.release_time,'%Y-%m-%d') = :date group by a.unit",
            nativeQuery = true)
    List<Object[]> getOtcTurnoverAmount(@Param("date") String date);

    // add by tansitao 时间： 2018/6/12 原因：合并保证金功能
    @Query(
            value =
                    "select sum(b.commission) as fee,sum(b.money) as money from Order b  where  b.status = 3 and b.memberId = :memberId")
    Map<String, Object> getBusinessStatistics(@Param("memberId") Long memberId);

    /**
     * 更新otcApiOrder状态
     *
     * @param status
     * @param orderId
     * @param oldStatus
     * @return
     */
    @Modifying
    @Query(
            value =
                    "update otc_api_order a set a.status=:status where a.status=:oldStatus and a.id=:orderId",
            nativeQuery = true)
    int updateOtcApiOrder(
            @Param("status") int status,
            @Param("orderId") Long orderId,
            @Param("oldStatus") int oldStatus);

    /**
     * 更新星客otcOrder状态
     *
     * @param status
     * @param orderId
     * @param oldStatus
     * @return
     */
    @Modifying
    @Query(
            value = "update otc_order a set a.status=:status where a.status=:oldStatus and a.id=:orderId",
            nativeQuery = true)
    int updateOldOtcOrder(
            @Param("status") int status,
            @Param("orderId") Long orderId,
            @Param("oldStatus") int oldStatus);

    /**
     * 取消otc-api-order
     *
     * @param date
     * @param status
     * @param orderSn
     * @param cancelMemberId
     * @return true
     * @author shenzucai
     * @time 2019.10.01 15:02
     */
    @Modifying
    @Query(
            value =
                    "update otc_api_order a set a.cancel_time=:date,a.status=:status,a.cancel_member_id=:cancelMemberId where a.order_sn=:orderSn and a.status = 1",
            nativeQuery = true)
    int cancelOrderByhandleBOA(
            @Param("date") Date date,
            @Param("status") Integer status,
            @Param("orderSn") String orderSn,
            @Param("cancelMemberId") long cancelMemberId);

    /**
     * otc-api资金变动
     *
     * @param date
     * @param amount
     * @param orderSn
     * @param sMemberId
     * @return true
     * @author shenzucai
     * @time 2019.10.01 15:18
     */
    @Modifying
    @Query(
            value =
                    "UPDATE otc_api_org_account SET balance = balance + :amount,frozen_amount = frozen_amount - :amount,update_time = :date WHERE member_id = :sMemberId AND frozen_amount >= :amount AND organization_id = (select organization_id from otc_api_order where order_sn = :orderSn limit 1)",
            nativeQuery = true)
    int cancelOrderBalanceByhandleBOA(
            @Param("date") Date date,
            @Param("amount") BigDecimal amount,
            @Param("orderSn") String orderSn,
            @Param("sMemberId") long sMemberId);

    /**
     * otc-api资金变动
     *
     * @param date
     * @param amount
     * @param orderSn
     * @param sMemberId
     * @return true
     * @author shenzucai
     * @time 2019.10.01 15:18
     */
    @Modifying
    @Query(
            value =
                    "UPDATE otc_api_org_account SET frozen_amount = frozen_amount - :amount,update_time = :date WHERE member_id = :sMemberId AND frozen_amount >= :amount AND organization_id = (select organization_id from otc_api_order where order_sn = :orderSn limit 1)",
            nativeQuery = true)
    int updateOrderBalanceByhandleBOA(
            @Param("date") Date date,
            @Param("amount") BigDecimal amount,
            @Param("orderSn") String orderSn,
            @Param("sMemberId") long sMemberId);

    @Modifying
    @Query(
            value =
                    "update otc_api_order a set a.pay_time=:date,a.status=:status,a.pay_method=:payMode,a.pay_method_info=:payModeInfo where a.status=1 and a.order_sn=:orderSn",
            nativeQuery = true)
    int updatePayOrderBOA(
            @Param("date") Date date,
            @Param("status") Integer status,
            @Param("orderSn") String orderSn,
            @Param("payMode") Integer payMode,
            @Param("payModeInfo") String payModeInfo);

    @Modifying
    @Query(
            value =
                    "insert into otc_api_org_account_transaction (id,org_account_id,symbol,amount,type,fee,ref_id,create_time,update_time) VALUES (:id,(select id from otc_api_org_account where member_id = :memberId and organization_id = (select organization_id from otc_api_order where order_sn = :orderSn limit 1) limit 1),'BT',:amount,:type,:fee,:orderSn,now(),now())",
            nativeQuery = true)
    int saveOtcApiTransaction(
            @Param("id") Long id,
            @Param("memberId") Long memberId,
            @Param("amount") BigDecimal amount,
            @Param("type") Integer type,
            @Param("fee") BigDecimal fee,
            @Param("orderSn") String orderSn);

    @Modifying
    @Query(
            value =
                    "update otc_api_order a set a.release_time=:date,a.status=:status where  (a.status=2 or a.status=4) and a.order_sn=:orderSn",
            nativeQuery = true)
    int releaseOrderBOA(
            @Param("date") Date date,
            @Param("status") Integer status,
            @Param("orderSn") String orderSn);


    /**
     * 确认付款
     * @param date
     * @param status
     * @param orderSn
     * @param payModeId
     * @param payModeInfo
     * @return
     */
    @Modifying
    @Query("update Order a set a.payTime=:date,a.status=:status,a.payMethod=:payModeId,a.payMethodInfo=:payModeInfo,a.attr1=:allPayInfo where a.status=1 and a.orderSn=:orderSn")
    int orderPayMethod(@Param("date") Date date, @Param("status")OrderStatus status, @Param("orderSn")String orderSn, @Param("payModeId")Long payModeId, @Param("payModeInfo")String payModeInfo, @Param("allPayInfo")String allPayInfo);

}
