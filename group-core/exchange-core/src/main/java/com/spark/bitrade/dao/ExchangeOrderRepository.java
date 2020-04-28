package com.spark.bitrade.dao;

import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.constant.ExchangeOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExchangeOrderRepository extends JpaRepository<ExchangeOrder, String>, JpaSpecificationExecutor<ExchangeOrder>, QueryDslPredicateExecutor<ExchangeOrder> {
    ExchangeOrder findByOrderId(String orderId);

    @Modifying
    @Query("update ExchangeOrder exchange set exchange.tradedAmount = exchange.tradedAmount + ?1  where exchange.orderId = ?2")
    int increaseTradeAmount(BigDecimal amount, String orderId);

    @Modifying
    @Query("update ExchangeOrder  exchange set exchange.status = :status where exchange.orderId = :orderId")
    int updateStatus(@Param("orderId") String orderId, @Param("status") ExchangeOrderStatus status);

    @Query(value="select coin_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit",nativeQuery = true)
    List<Object[]> getExchangeTurnoverCoin(@Param("date") String date);

    @Query(value="select base_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(turnover) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit",nativeQuery = true)
    List<Object[]> getExchangeTurnoverBase(@Param("date") String date);

    @Query(value="select base_symbol , coin_symbol,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount),sum(turnover) from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by base_symbol,coin_symbol",nativeQuery = true)
    List<Object[]> getExchangeTurnoverSymbol(@Param("date") String date) ;


    /**
     * 撤销订单
     *
     * @param orderId 订单号
     * @param tradeAmount 交易数量
     * @param turnover 成交额
     * @param canceledTime 撤单时间
     * @return
     */
    //撤销状态为交易中的订单
    @Modifying
    @Query("update ExchangeOrder e set e.status = 2,e.tradedAmount=:tradeAmount, e.turnover=:turnover, e.canceledTime=:canceledTime where e.orderId = :orderId and e.status=0")
    int cancelOrder(@Param("orderId") String orderId, @Param("tradeAmount") BigDecimal tradeAmount, @Param("turnover") BigDecimal turnover , @Param("canceledTime") long canceledTime);


    /**
     * 完成订单
     *
     * @param orderId 订单号
     * @param tradeAmount 交易数量
     * @param turnover 成交额
     * @param completedTime 完成时间
     * @return
     */
    //完成状态为交易中的订单
    @Modifying
    @Query("update ExchangeOrder e set e.status = 1,e.tradedAmount=:tradeAmount, e.turnover=:turnover, e.completedTime=:completedTime where e.orderId = :orderId and e.status=0")
    int completedOrder(@Param("orderId") String orderId, @Param("tradeAmount") BigDecimal tradeAmount, @Param("turnover") BigDecimal turnover , @Param("completedTime") long completedTime);
}
