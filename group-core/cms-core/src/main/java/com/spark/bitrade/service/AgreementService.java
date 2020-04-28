package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.dao.AgreementDao;
import com.spark.bitrade.dto.AgreementDto;
import com.spark.bitrade.entity.Agreement;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
  * 协议service
  * @author tansitao
  * @time 2018/4/25 14:36 
  */
@Service
public class AgreementService extends BaseService<Agreement> {
    @Autowired
    private AgreementDao agreementDao;

    @CacheEvict(cacheNames = "agreement", allEntries = true)
    public Agreement save(Agreement agreement) {
        return agreementDao.save(agreement);
    }

    @Override
    @Cacheable(cacheNames = "agreement", key = "'entity:agreement:All'")
    public List<Agreement> findAll() {
        return agreementDao.findAll();
    }

    @Cacheable(cacheNames = "agreement", key = "'entity:agreement:All-'+#isShow")
    public List<Agreement> findAllByisShow(boolean isShow) {
        return agreementDao.findAllByisShow(isShow);
    }

    @Cacheable(cacheNames = "agreement", key = "'entity:agreement:'+#id")
    public Agreement findById(Long id) {
        return agreementDao.findOne(id);
    }

    @CacheEvict(cacheNames = "agreement", key = "'entity:agreement:'+#id")
    public void deleteById(Long id) {
        agreementDao.delete(id);
    }

    @CacheEvict(cacheNames = "agreement", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            Agreement agreement = findById(id);
            Assert.notNull(agreement, "validate id!");
            deleteById(id);
        }
    }

    public int getMaxSort() {
        return agreementDao.findMaxSort();
    }

    public Page<Agreement> findAll(Predicate predicate, Pageable pageable) {
        return agreementDao.findAll(predicate, pageable);
    }

    /**
     * 上币中心
     *
     * @param appId 终端类型，0-web;1-app
     * @return
     */
    public Map<String, Object> currencyCenter(Integer appId) {
        Map<String, Object> map = new HashMap<>(2);
        // 创新区
        Agreement innovationZone = this.findById(17L);
        map.put("innovationZone", innovationZone);
        if (appId == 0) {
            // 主区
            Agreement mainArea = this.findById(11L);
            map.put("mainArea", mainArea);
        }
        return map;
    }
}
