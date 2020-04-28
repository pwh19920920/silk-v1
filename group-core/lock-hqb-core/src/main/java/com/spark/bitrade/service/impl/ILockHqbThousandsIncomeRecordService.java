package com.spark.bitrade.service.impl;

import com.spark.bitrade.vo.AnnualRateOfWeekVO;
import com.spark.bitrade.entity.LockHqbThousandsIncomeRecord;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Zhang Yanjun
 * @time 2019.04.23 16:34
 */
public interface ILockHqbThousandsIncomeRecordService extends IService<LockHqbThousandsIncomeRecord> {

    /**
     * 昨日万份收益接口
     *
     * @param appId
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 11:26
     */
    LockHqbThousandsIncomeRecord yesterdayThousandsIncome(String appId, String unit, Integer time);

    /**
     * 最近一个月的万份收益(30天)
     *
     * @param appId
     * @param unit
     * @author Zhang Yanjun
     * @time 2019.04.24 11:39
     */
    List<LockHqbThousandsIncomeRecord> lastMonthThousandsIncome(String appId, String unit, Integer limit);

    /**
     * 获取日年化率
     *
     * @param coinSymbol
     * @param date
     * @return
     * @author dengdy
     */
    BigDecimal getAnnulYieldOfDay(String coinSymbol, Date date, String appId);

    /**
     * 获取过去7天平均日利率
     *
     * @param lastDay    是计算利率，7天中的最后一天
     * @param coinSymbol
     * @param appId
     * @return
     */
    BigDecimal getAverageAnnulYieldOfWeek(Date lastDay, String coinSymbol, String appId);

    /**
     * 获取当天万份收益
     *
     * @param symbol
     * @param appId
     * @param date 日期
     * @return
     * @author dengdy
     */
    LockHqbThousandsIncomeRecord getTenThousandIncomeToday(String symbol, String appId, Date date);

    /**
     * 查询七日年化率Vo
     *
     * @param memberId
     * @param symbol
     * @param appId
     * @param queryDate
     * @return
     */
    AnnualRateOfWeekVO getAnnualRateOfWeekVO(Long memberId, String symbol, String appId, Date queryDate);

    /**
     * 查询30天內七日年化率Vo
     *
     * @param memberId
     * @param symbol
     * @param appId
     * @param currentDay
     * @return
     */
    List<AnnualRateOfWeekVO> getRecentMonthAnnualRateOfWeekVO(Long memberId, String symbol, String appId, Date currentDay);

}
