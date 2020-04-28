package com.spark.bitrade.service;


import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.LockBccAssignUnlock;


/**
 * (LockBccAssignUnlock)表服务接口
 *
 * @author fatKarin
 * @since 2019-03-19 15:40:21
 */

public interface LockBccAssignUnlockService extends IService<LockBccAssignUnlock>{

    /**
     * 分页查询解仓记录
     *
     * @param memberId
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageInfo<LockBccAssignUnlock> findLockBccAssignUnlocksForPage(Long memberId, Integer pageNo, Integer pageSize);
}