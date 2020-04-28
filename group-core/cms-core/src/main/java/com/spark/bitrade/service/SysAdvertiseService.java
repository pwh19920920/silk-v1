package com.spark.bitrade.service;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysAdvertiseLocation;
import com.spark.bitrade.dao.SysAdvertiseDao;
import com.spark.bitrade.entity.QSysAdvertise;
import com.spark.bitrade.entity.SysAdvertise;
import com.spark.bitrade.mapper.dao.SysAdvertiseMapper;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.spark.bitrade.entity.QSysAdvertise.sysAdvertise;

/**
 * @author rongyu
 * @description 系统广告service
 * @date 2018/1/6 16:45
 */
@Service
public class SysAdvertiseService extends BaseService {
    @Autowired
    SysAdvertiseDao sysAdvertiseDao;
    @Autowired
    SysAdvertiseMapper advertiseMapper;


    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param predicateList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<SysAdvertise> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<SysAdvertise> list;
        JPAQuery<SysAdvertise> jpaQuery = queryFactory.selectFrom(sysAdvertise);
        if (predicateList != null)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, sysAdvertise.createTime))
                    .offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, sysAdvertise.createTime)).fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    @Cacheable(cacheNames = "sysAdvertise", key = "'entity:sysAdvertise:'+#serialNumber")
    public SysAdvertise findOne(String serialNumber) {
        return sysAdvertiseDao.findOne(serialNumber);
    }

    public int getMaxSort() {
        return sysAdvertiseDao.findMaxSort();
    }

    //edit by tansitao 时间： 2018/8/16 原因：保存广告，清空缓存
    @CacheEvict(cacheNames = "sysAdvertise", allEntries = true)
    public SysAdvertise save(SysAdvertise sysAdvertise) {
        return sysAdvertiseDao.save(sysAdvertise);
    }

    @Cacheable(cacheNames = "sysAdvertise", key = "'entity:sysAdvertise:all'")
    public List<SysAdvertise> findAll() {
        return sysAdvertiseDao.findAll();
    }

    @CacheEvict(cacheNames = "sysAdvertise", key = "'entity:sysAdvertise:'+#serialNumber")
    public void deleteOne(String serialNumber) {
        sysAdvertiseDao.delete(serialNumber);
    }

    @CacheEvict(cacheNames = "sysAdvertise", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String[] array) {
        for (String serialNumber : array) {
            sysAdvertiseDao.delete(serialNumber);
        }
    }

    @Cacheable(cacheNames = "sysAdvertise", key = "'entity:sysAdvertise:all-'+#sysAdvertiseLocation.name()")
    public List<SysAdvertise> findAllNormal(SysAdvertiseLocation sysAdvertiseLocation) {
        return sysAdvertiseDao.findAllByStatusAndSysAdvertiseLocationOrderBySort(CommonStatus.NORMAL, sysAdvertiseLocation);
    }

    public List<SysAdvertise> findAll(List<Predicate> predicateList) {
        List<SysAdvertise> list;
        JPAQuery<SysAdvertise> jpaQuery = queryFactory.selectFrom(QSysAdvertise.sysAdvertise);
        if (predicateList != null) {
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        }
        list = jpaQuery.orderBy(new OrderSpecifier<>(Order.DESC, QSysAdvertise.sysAdvertise.createTime)).fetch();
        return list;
    }

    public Page<SysAdvertise> findAll(Predicate predicate, Pageable pageable) {
        return sysAdvertiseDao.findAll(predicate, pageable);
    }

    /**
     * 获取是否存在已经上线的APP活动位置的广告
     *
     * @param adType     广告类型（0：web，1:app）
     * @param adLocation 广告位置
     * @param status     广告状态（0上线，1下线）
     * @return true
     * @author fumy
     * @time 2018.11.19 13:52
     */
    @ReadDataSource
    public boolean isExistAppActNormalAd(int adType, int adLocation, int status) {
        int row = advertiseMapper.isExistAppActNormalAd(adType, adLocation, status);
        return row > 0 ? true : false;
    }


    /**
     * 根据广告类型、广告位置获取已经启用的系统广告列表
     *
     * @param
     * @return true
     * @author fumy
     * @time 2018.11.19 14:55
     */
    @Cacheable(cacheNames = "sysAdvertise", key = "'entity:sysAdvertise:all-'+#type+'-'+#sysAdvertiseLocation.name()+'-'+#languageCode")
    public List<SysAdvertise> findNormalAdByTypeAndLocation(int type, SysAdvertiseLocation sysAdvertiseLocation, String languageCode) {
        return advertiseMapper.queryNormalAdByTypeAndLocation(type, sysAdvertiseLocation.getOrdinal(), languageCode);
    }

}
