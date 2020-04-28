package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.FincPlatStat;


import java.util.List;

/**
 * <p>
 * 平台货币统计表 服务类
 * </p>
 *
 * @author shenzucai
 * @since 2018-05-13
 */
public interface IFincPlatStatService extends IService<FincPlatStat> {

    /**
     * 获取T+1模式前的数据（前一天）
     * @author shenzucai
     * @time 2018.05.13 15:50
     * @param
     * @return true
     */
    List<FincPlatStat> listPlatStat(String startTime,String endTime);
}
