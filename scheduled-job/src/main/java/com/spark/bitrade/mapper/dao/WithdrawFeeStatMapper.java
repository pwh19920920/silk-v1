package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.WithdrawFeeStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 提币手续费统计表 Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2018-06-13
 */
@Mapper
public interface WithdrawFeeStatMapper extends BaseMapper<WithdrawFeeStat> {

    /**
    * 获取前一天的数据
    * @author fumy
    * @time 2018.06.13 14:53
     * @param
    * @return true
    */
    List<WithdrawFeeStat> listFeeTotal(@Param("startTime") String startTime,@Param("endTime") String endTime);

    /**
    * 获取每日总提币手续费统计数据
    * @author fumy
    * @time 2018.06.13 17:38
     * @param
    * @return true
    */
    List<WithdrawFeeStat> dayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime);

    String getMaxOpDate();

    int insertOrUpdate(WithdrawFeeStat wfs);

    int updateTotal(WithdrawFeeStat wfs);

    //////////////////////////////////////////操盘手手续费统计

    List<WithdrawFeeStat> traderListFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime,
                                            @Param("traderMemberId") String traderMemberId);

    List<WithdrawFeeStat> traderDayOfFeeStat(@Param("startTime") String startTime,@Param("endTime") String endTime,
                                             @Param("traderMemberId") String traderMemberId);

    String getTraderMaxOpDate();

    int traderInsertAndUpdate(WithdrawFeeStat wfs);

    int traderUpdateTotal(WithdrawFeeStat wfs);

    int traderInsert(WithdrawFeeStat wfs);

    int traderCount();
}
