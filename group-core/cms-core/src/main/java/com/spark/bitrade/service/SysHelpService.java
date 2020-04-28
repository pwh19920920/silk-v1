package com.spark.bitrade.service;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.SysHelpClassification;
import com.spark.bitrade.dao.SysHelpDao;
import com.spark.bitrade.entity.QSysHelp;
import com.spark.bitrade.entity.SysHelp;
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
 * @description
 * @date 2018/1/9 10:00
 */
@Service
public class SysHelpService extends BaseService {
    @Autowired
    private SysHelpDao sysHelpDao;

    @CacheEvict(cacheNames = "sysHelp",  allEntries = true)
    public SysHelp save(SysHelp sysHelp) {
        return sysHelpDao.save(sysHelp);
    }

    @Cacheable(cacheNames = "sysHelp", key = "'entity:sysHelp:all'")
    public List<SysHelp> findAll() {
        return sysHelpDao.findAll();
    }

    @Cacheable(cacheNames = "sysHelp", key = "'entity:sysHelp:all-'+#status")
    public List<SysHelp>findAllByStatus(CommonStatus status){
        return sysHelpDao.findAllByStatus(status);
    }

    @Cacheable(cacheNames = "sysHelp", key = "'entity:sysHelp:'+#id")
    public SysHelp findOne(Long id) {
        return sysHelpDao.findOne(id);
    }

    @CacheEvict(cacheNames = "sysHelp", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            sysHelpDao.delete(id);
        }
    }

    public int getMaxSort(){
        return sysHelpDao.findMaxSort();
    }

    @Cacheable(cacheNames = "sysHelp", key = "'entity:sysHelp:all-'+#sysHelpClassification")
    public List<SysHelp> findBySysHelpClassification(SysHelpClassification sysHelpClassification) {
        return sysHelpDao.findAllBySysHelpClassification(sysHelpClassification);
    }

    @Cacheable(cacheNames = "sysHelp",key = "'entity:sysHelp:all-'+#sysHelpClassification+'&'+#status")
    public List<SysHelp> findAllByStatusAndSysHelpClassification(CommonStatus status,SysHelpClassification sysHelpClassification){
        return sysHelpDao.findAllByStatusAndSysHelpClassification(status,sysHelpClassification);
    }
    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<SysHelp> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<SysHelp> jpaQuery = queryFactory.selectFrom(QSysHelp.sysHelp);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(QSysHelp.sysHelp.createTime.desc());
        List<SysHelp> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        PageResult<SysHelp> page = new PageResult<>(list, pageNo, pageSize, count);
        return page;
    }

    public Page<SysHelp> findAll(Predicate predicate, Pageable pageable) {
        return sysHelpDao.findAll(predicate, pageable);
    }
}
