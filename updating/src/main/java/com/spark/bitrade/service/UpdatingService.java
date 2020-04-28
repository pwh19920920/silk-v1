package com.spark.bitrade.service;

import com.spark.bitrade.constant.Platform;
import com.spark.bitrade.dao.AppRevisionDao;
import com.spark.bitrade.dao.UpdatingDao;
import com.spark.bitrade.entity.AppRevision;
import com.spark.bitrade.entity.Updating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Author: yangch
 * Version: 1.0
 * Create Date Time: 2018-05-29 22:27:00
 * Update Date Time:
 *
 * @see
 */

@Service
public class UpdatingService {

    private static boolean isUpdating = false; //在内存中存放是否升级的标志，不用实时的刷新数据库

    @Autowired
    private UpdatingDao updatingDao;

    @Autowired
    private AppRevisionDao appRevisionDao;

    public Updating save(Updating updating) {
        return updatingDao.save(updating);
    }

    public Updating findOne(Integer id){
        return updatingDao.findOne(id);
    }

    /**
     * 是否升级（缓存的结果）
     *
     * @return
     */
    public boolean isUpdating(){
        return isUpdating;
    }

    /**
     * 刷新是否升级的状态
     * @return
     */
    public boolean flushUpdatingStatus(){
        if(updatingDao.countByFlagEquals(1)>0){
            isUpdating = true;
            return true;
        } else {
            isUpdating = false;
            return false;
        }
    }

    /**
     * 查询最后一条升级内容
     * @return
     */
    public Updating queryByIdDesc() {
        return  updatingDao.findFirstByFlagOrderByIdDesc(1);
    }


    @Cacheable(cacheNames = "appRevision", key = "'entity:appRevision:'+#platform.name()")
    public AppRevision findAppRevisionByPlatformOrderByIdDesc(Platform platform){
        return appRevisionDao.findAppRevisionByPlatformOrderByIdDesc(platform.getOrdinal());
    }

}
