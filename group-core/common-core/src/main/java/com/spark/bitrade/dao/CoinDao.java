package com.spark.bitrade.dao;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dto.CoinDto;
import com.spark.bitrade.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author rongyu
 * @description 货币操作
 * @date 2017/12/29 14:41
 */
@Repository
public interface CoinDao extends JpaRepository<Coin, String>, JpaSpecificationExecutor<Coin>, QueryDslPredicateExecutor<Coin> {
    Coin findByUnit(String unit);

    List<Coin> findAllByCanWithdrawAndStatusAndHasLegal(BooleanEnum is, CommonStatus status, boolean hasLegal);

    Coin findByUnitAndCanWithdrawAndStatusAndHasLegal(String unit,BooleanEnum is, CommonStatus status, boolean hasLegal);

    Coin findCoinByIsPlatformCoin(BooleanEnum is);

    List<Coin> findByHasLegal(Boolean hasLegal);

    @Query("select a from Coin a where a.unit in (:units) ")
    List<Coin> findAllByOtc(@Param("units") List<String> otcUnits);

    @Query("select a.name from Coin a")
    List<String> findAllName();

    //add by yangch 时间： 2018.04.29 原因：合并
    @Query(value = "select  new com.spark.bitrade.dto.CoinDto(a.name,a.unit) from Coin a")
    List<CoinDto> findAllNameAndUnit();

    @Query("select a.name from Coin a where a.hasLegal = true ")
    List<String> findAllCoinNameLegal();

    /*
 * 查询出所有主币（非代币类型）
 * @author shenzucai
 * @time 2018.04.22 11:33
 * @param baseCoinUnit
 * @return true
 */
    @Query("select a from Coin a where a.baseCoinUnit is null or a.baseCoinUnit = ''")
    List<Coin> findAllByBaseCoinUnit();
    //add|edit|del by shenzucai 时间： 2018.05.25 原因：修改该方法

    @Query("SELECT c FROM Coin c ORDER BY c.sort ASC")
    List<Coin> findCoinOrderBySort();

    //查询币种介绍
    @Query("select a.content from Coin a where a.unit=:unit")
    String findContentByUnit(@Param("unit") String unit);
}
