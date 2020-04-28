package com.spark.bitrade.service;


import com.baomidou.mybatisplus.service.IService;
import com.spark.bitrade.entity.LockMarketExtraRewardConfig;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (LockMarketExtraRewardConfig)表服务接口
 *
 * @author makejava
 * @since 2019-03-19 15:40:21
 */

public interface LockMarketExtraRewardConfigService extends IService<LockMarketExtraRewardConfig> {

  /*  *//**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     *//*
    LockMarketExtraRewardConfig queryById(Long id);

    *//**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     *//*
    List<LockMarketExtraRewardConfig> queryAllByLimit(int offset, int limit);

    *//**
     * 新增数据
     *
     * @param lockMarketExtraRewardConfig 实例对象
     * @return 实例对象
     *//*
    boolean insert(LockMarketExtraRewardConfig lockMarketExtraRewardConfig);

    *//**
     * 修改数据
     *
     * @param lockMarketExtraRewardConfig 实例对象
     * @return 实例对象
     *//*
    LockMarketExtraRewardConfig update(LockMarketExtraRewardConfig lockMarketExtraRewardConfig);

    *//**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     *//*
    boolean deleteById(Long id);
*/

    /**
     * 查询所有有效配置
     * @return
     */
    List<LockMarketExtraRewardConfig> getActivityLists();
}