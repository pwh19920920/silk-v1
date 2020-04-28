package com.spark.bitrade.mapper.dao;

import com.spark.bitrade.entity.PlatInstation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 运营手动编辑站内信
 *
 * @author Zhongxj
 * @time 2019.09.29
 */
@Mapper
public interface PlatInstationMapper {
    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    PlatInstation findById(@Param("id") Long id);

    /**
     * 修改状态为，发送失败
     *
     * @param id
     * @return
     */
    PlatInstation updatePlatInstation(@Param("id") Long id);
}
