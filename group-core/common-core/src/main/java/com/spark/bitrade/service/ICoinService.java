package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.SilkDataDist;
import org.apache.ibatis.annotations.Param;

/**
 * @author Zhang Yanjun
 * @time 2019.02.27 15:24
 */
public interface ICoinService extends IService<Coin> {
    Coin findByUnit(String unit);
}
