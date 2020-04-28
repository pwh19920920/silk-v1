package com.spark.bitrade.mapper.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.spark.bitrade.entity.FincPlatStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 平台货币统计表 Mapper 接口
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-13
 */
@Mapper
public interface FincPlatStatMapper extends BaseMapper<FincPlatStat> {

    /**
     * 获取前的数据（前一天18:00:00 至当天18:00:00）
     * @author shenzucai
     * @time 2018.05.13 15:50
     * @param
     * @return true
     */
    List<FincPlatStat> listPlatStat(@Param("startTime") String startTime, @Param("endTime") String endTime,
                                    @Param("traderMemberId") String traderMemberId,@Param("innerMemberId") String innerMemberId,
                                    @Param("employeeMemberId") String employeeMemberId);
}
