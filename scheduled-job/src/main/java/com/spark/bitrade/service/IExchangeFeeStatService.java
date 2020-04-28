package com.spark.bitrade.service;

import com.spark.bitrade.entity.ExchangeFeeStat;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fumy
 * @since 2018-06-16
 */
public interface IExchangeFeeStatService extends IService<ExchangeFeeStat> {

   /**
   * 统计前一天的币币交易数据
   * @author fumy
   * @time 2018.06.16 12:00
    * @param
   * @return true
   */
    List<ExchangeFeeStat> exchangeFeeTotal(String startTime,String endTime);

    /**
    * 统计每日币币交易手续费数据
    * @author fumy
    * @time 2018.06.16 12:00
     * @param
    * @return true
    */
    List<ExchangeFeeStat> dayOfFeeStat(String startTime,String endTime);

    String getMaxOpDate();

    int insertAndUpdate(ExchangeFeeStat efs);

    int updateTotal(ExchangeFeeStat efs);


    ////////////////////////////////////// 操盘手 币币交易手续费统计

    List<ExchangeFeeStat> traderExchangeFeeTotal(String startTime,String endTime);

    /**
     * 统计每日币币交易手续费数据
     * @author fumy
     * @time 2018.06.16 12:00
     * @param
     * @return true
     */
    List<ExchangeFeeStat> traderDayOfFeeStat(String startTime,String endTime);

    String getTraderMaxOpDate();

    int traderInsertAndUpdate(ExchangeFeeStat efs);

    int traderUpdateTotal(ExchangeFeeStat efs);

    int selectTraderTotalCount();

    boolean traderInsert(ExchangeFeeStat efs);

}
