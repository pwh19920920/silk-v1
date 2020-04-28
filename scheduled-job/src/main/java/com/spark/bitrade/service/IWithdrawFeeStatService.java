package com.spark.bitrade.service;

import com.spark.bitrade.entity.WithdrawFeeStat;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 * 提币手续费统计表 服务类
 * </p>
 *
 * @author fumy
 * @since 2018-06-13
 */
public interface IWithdrawFeeStatService extends IService<WithdrawFeeStat> {

    /**
    * 获取前一天的数据
    * @author fumy
    * @time 2018.06.13 14:46
     * @param
    * @return true
    */
    List<WithdrawFeeStat> listFeeStat(String startTime,String endTime);

    /***
    * 获取每日总提币手续费统计数据
    * @author fumy
    * @time 2018.06.13 17:38
     * @param
    * @return true
    */
    List<WithdrawFeeStat> dayOfFeeStat(String startTime,String endTime);

    String getMaxOpDate();

    int insertAndUpdate(WithdrawFeeStat wfs);

    int updateTotal(WithdrawFeeStat wfs);

    //////////////////////////操盘手提币手续费统计

    List<WithdrawFeeStat> traderListFeeStat(String startTime,String endTime);

    /***
     * 获取每日提币手续费统计数据
     * @author fumy
     * @time 2018.06.13 17:38
     * @param
     * @return true
     */
    List<WithdrawFeeStat> traderDayOfFeeStat(String startTime,String endTime);

    String getTraderMaxOpDate();

    int traderInsertAndUpdate(WithdrawFeeStat wfs);

    int traderUpdateTotal(WithdrawFeeStat wfs);

    int selectTraderTotalCount();

    boolean traderInsert(WithdrawFeeStat wfs);
}
