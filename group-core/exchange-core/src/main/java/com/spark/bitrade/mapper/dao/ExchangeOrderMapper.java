package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.ExchangeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
  * 币币交易订单 mybatis数据接口
 *
  * @author yangch
  * @time 2018.06.22 14:40
  */

@Mapper
public interface ExchangeOrderMapper {

    /**
     * 查询当前委托订单
     *
     * @param symbol
     * @param memberId
     * @param coinSymbol 交易币种
     * @param baseSymbol 基币
     * @param direction  交易类型
     * @param status     委托状态
     * @return
     */
    List<ExchangeOrder> queryCurrent(@Param("symbol") String symbol,
                                     @Param("memberId") Long memberId,
                                     @Param("coinSymbol") String coinSymbol,
                                     @Param("baseSymbol") String baseSymbol,
                                     @Param("direction") Integer direction,
                                     @Param("status") Integer status);

    /**
     * 查询历史委托订单
     *
     * @param symbol
     * @param memberId
     * @param coinSymbol 交易币种
     * @param baseSymbol 基币
     * @param direction  交易类型
     * @param status     委托状态
     * @return
     */
    List<ExchangeOrder> queryHistory(@Param("symbol") String symbol,
                                     @Param("memberId") Long memberId,
                                     @Param("coinSymbol") String coinSymbol,
                                     @Param("baseSymbol") String baseSymbol,
                                     @Param("direction") Integer direction,
                                     @Param("status") Integer status);

    /**
     * 查询所有订单
     *
     * @param symbol   可选，交易对
     * @param memberId 必填，会员ID
     * @return
     */
    List<ExchangeOrder> queryAll(@Param("symbol") String symbol, @Param("memberId") Long memberId);

    /**
     * 订单查询
     *
     * @param orderId
     * @return
     */
    ExchangeOrder queryByOrderId(@Param("orderId") String orderId);

    /**
     * 查询撤单次数
     *
     * @param symbol
     * @param memberId
     * @param startTick
     * @param endTick
     * @return
     */
    Integer findTodayOrderCancelTimes(@Param("symbol") String symbol, @Param("memberId") Long memberId, @Param("startTick") long startTick, @Param("endTick") long endTick);

    /**
     * 查询超时订单
     *
     * @param symbol
     * @param overtimeTick
     * @return
     */
    List<ExchangeOrder> findOvertimeOrder(@Param("symbol") String symbol, @Param("overtimeTick") Long overtimeTick);
}
