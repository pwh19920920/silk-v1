package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.constant.OrderStatus;
import com.spark.bitrade.dto.OtcApiOrderDto;
import com.spark.bitrade.entity.Order;
import com.spark.bitrade.entity.OtcApiAppeal;
import com.spark.bitrade.vo.MyOrderVO;
import com.spark.bitrade.vo.OtcOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *  * 法币交易订单mapper
 *  * @author tansitao
 *  * @time 2018/6/22 10:54 
 *  
 */
@Mapper
public interface OtcOrderMapper {

    //查询今天撤单数量
    int findTodayAllCancel(@Param("customerId") long customerId, @Param("limitTime") String limitTime);

    //add 时间： 2018/5/14 原因：查询所有未完成的订单数量
    @Select("SELECT COUNT(1) from otc_order o where o.customer_id = #{customerId} and (o.status=1 or o.status=2)")
    int findUnFinishNum(@Param("customerId") long customerId);

    //查询超时的订单号 modify by qhliao 不查询出otc-api的订单
    @Select("select a.order_sn from otc_order a where timestampdiff(MINUTE,a.create_time,#{date})>=a.time_limit and a.status=1 and order_source_type != 88888888")
    List<String> findAllExpiredOrderSn(@Param("date") Date date);

    //add 时间： 2018/12/26 原因：查询所有未完成的订单
    @Select("SELECT * from otc_order o where o.customer_id = #{customerId} and o.member_id = #{memberId} and (o.status=1 or o.status=2 or o.status=4)")
    List<Order> findUnFinishOder(@Param("memberId") long memberId);

    List<Map<String, Long>> findCountByMembers(@Param("memberIds") Long[] memberIds, @Param("type") Integer type);

    List<Map<String, Long>> selectCountByMembersAnd48(@Param("memberIds") Long[] memberIds, @Param("type") Integer type, @Param("date") Date date);

    List<MyOrderVO> findOrderBy(@Param("memberId") long memberId, @Param("status") int status, @Param("orderSn") String orderSn,
                                @Param("unit") String unit, @Param("type") int type, @Param("money") BigDecimal money);

    /**
     *  * 根据应用ID/渠道ID查询流水记录（注意：仅为客户的流水记录）
     *  * @author yangch
     *  * @time 2019.03.07 19:16 
     *
     * @param memberId  用户ID
     * @param appId     应用ID/渠道ID
     * @param startTime 开始时间，可选
     * @param endTime   截至时间，可选
     *                   
     */
    List<OtcOrderVO> findRecordByUidAndAppId(@Param("coinId") Long coinId, @Param("memberId") Long memberId, @Param("appId") Long appId,
                                             @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select * from otc_api_order where order_sn=#{orderSn}")
    OtcApiOrderDto findOtcApiOrderByorderSn(@Param("orderSn") String orderSn);

    @Select("select * from otc_api_order where id=#{orderId}")
    OtcApiOrderDto findOtcApiOrderByorderId(@Param("orderId") Long orderId);

    @Select("select * from otc_api_appeal where otc_api_order_id=#{orderId} order by create_time desc limit 1")
    OtcApiAppeal findAppealByOtcApiOrder(@Param("orderId") Long orderId);

    //查询即将超时的订单
    @Select("select a.order_sn from otc_order a where a.time_limit-timestampdiff(MINUTE,a.create_time,NOW())<=#{times} and a.time_limit-timestampdiff(MINUTE,a.create_time,NOW())>=0 and a.status=1 and (order_source_type != 88888888 or order_source_type is null)")
    List<String> findAllExpireRemindOrder(@Param("times") Long times);
}
