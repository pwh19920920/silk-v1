package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.LockCoinActivitieType;
import com.spark.bitrade.constant.LockSettingStatus;
import com.spark.bitrade.dao.LockCoinActivitieSettingDao;
import com.spark.bitrade.entity.LockCoinActivitieSetting;
import com.spark.bitrade.mapper.dao.LockCoinMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/***
 * 锁仓活动配置
 * @author yangch
 * @time 2018.06.12 16:43
 */

@Service
public class LockCoinActivitieSettingService extends BaseService {

    @Autowired
    private LockCoinActivitieSettingDao dao;

    @Autowired
    private LockCoinMapper mapper;

    @CacheEvict(cacheNames = "lockCoinActivitieSetting", allEntries = true)
    public LockCoinActivitieSetting save(LockCoinActivitieSetting entity){
        return dao.save(entity);
    }

    @Cacheable(cacheNames = "lockCoinActivitieSetting", key = "'entity:lockCoinActivitieSetting:'+#id")
    public LockCoinActivitieSetting findOne(Long id){
        return dao.findOne(id);
    }

    public int updateBoughtAmount(BigDecimal amount, Long id){
        return dao.updateLockCoinActivitieSettingsById(amount, id);
    }

    /**
     * 通过时间查找生效中的活动配置
     * @author tansitao
     * @time 2018/7/2 9:32 
     */
    @Cacheable(cacheNames = "lockCoinActivitieSetting", key = "'entity:lockCoinActivitieSetting:bytime-'+#id")
    @ReadDataSource
    public LockCoinActivitieSetting findOneByTime(Long id){
        return mapper.findByIdAndTime(id);
    }

    /**
      * 通过时间查找生效中的活动配置,不生成缓存
      * @author fatKarin
      * @time 2019/6/6 13:32 
      */
    @ReadDataSource
    public LockCoinActivitieSetting findOneByTimeWithoutCache(Long id){
        return mapper.findByIdAndTime(id);
    }

    /**
     * 分页查询
     * @author tansitao
     * @time 2018/6/13 10:33 
     */
    public Page<LockCoinActivitieSetting> findAll(Predicate predicate, Pageable pageable) {
        return dao.findAll(predicate, pageable);
    }

    /**
     * 通过activitieId获取所有子活动配置
     * @author tansitao
     * @time 2018/6/14 14:01 
     */
    public List<LockCoinActivitieSetting> findByActivitieId(long activitieId)
    {
       return dao.findByActivitieIdAndStatusOrderByLockDays(activitieId, LockSettingStatus.VALID);
    }

    public List<LockCoinActivitieSetting> findByTypeAndStatus(LockCoinActivitieType type,LockSettingStatus status){
        return dao.findLockCoinActivitieSettingsByTypeAndStatusEquals(type,status);
    }

    public List<LockCoinActivitieSetting> findQuantifyLock(){
        return dao.findLockCoinActivitieSettingsByTypeAndStatusAndActivitieId();
    }
}
