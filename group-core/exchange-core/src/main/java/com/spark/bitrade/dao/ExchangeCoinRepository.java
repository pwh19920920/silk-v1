package com.spark.bitrade.dao;

import com.spark.bitrade.constant.ExchangeCoinDisplayArea;
import com.spark.bitrade.entity.ExchangeCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangeCoinRepository extends JpaRepository<ExchangeCoin, String>, JpaSpecificationExecutor<ExchangeCoin>, QueryDslPredicateExecutor<ExchangeCoin> {
    ExchangeCoin findBySymbol(String symbol);
    //ExchangeCoin findFirstBySymbol(String symbol);

    //edit by yangch 时间： 2018.05.24 原因：未同步
    //@Query("select distinct a.baseSymbol from  ExchangeCoin a where a.enable = 1")
    @Query("select a.baseSymbol from  ExchangeCoin a group by a.baseSymbol")
    List<String> findBaseSymbol();

    //add by yangch 时间： 2018.05.24 原因：合并新增
    @Query("select distinct a.coinSymbol from  ExchangeCoin a where a.enable = 1 and a.baseSymbol = :baseSymbol")
    List<String> findCoinSymbol(@Param("baseSymbol")String baseSymbol);

    //add by yangch 时间： 2018.05.31 原因：代码合并
    @Query("select distinct a.coinSymbol from  ExchangeCoin a where a.enable = 1")
    List<String> findAllCoinSymbol();

    //根据指定的区域查询币种
    @Query("select a from  ExchangeCoin a where a.enable = 1 and a.displayArea = :displayArea order by a.sort")
    List<ExchangeCoin> queryAllByDisplayArea(@Param("displayArea")ExchangeCoinDisplayArea displayArea);

    //查询不显示的币种
    List<ExchangeCoin> findByIsShow(Integer isShow);
}
