package com.spark.bitrade.service;

import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.SilkDataDist;

import java.util.List;

/**
 * @author Zhang Yanjun
 * @time 2019.02.27 15:24
 */
public interface ISilkDataDistService  extends IService<SilkDataDist> {

    SilkDataDist findByIdAndKey(String id, String key);

    SilkDataDist findByKey(String key);

    Boolean toBoolean(SilkDataDist silkData);

    /**
     * 查询多个活动
     * @param id
     * @param key
     * @return
     */
    List<SilkDataDist> findListByIdAndKey(String id, String key);
}
