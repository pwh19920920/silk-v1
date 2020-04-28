package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.dao.AnnouncementDao;
import com.spark.bitrade.entity.Announcement;
import com.spark.bitrade.mapper.dao.AnnouncementMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author rongyu
 * @description
 * @date 2018/3/5 15:24
 */
@Service
public class AnnouncementService extends BaseService<Announcement> {
    @Autowired
    private AnnouncementDao announcementDao;
    @Autowired
    private AnnouncementMapper announcementMapper;


    @CacheEvict(cacheNames = "announcement", allEntries = true)
    public Announcement save(Announcement announcement) {
        return announcementDao.save(announcement);
    }

    @Cacheable(cacheNames = "announcement", key = "'entity:announcement:all'")
    public List<Announcement> findAll() {
        return announcementDao.findAll();
    }

    @Cacheable(cacheNames = "announcement", key = "'entity:announcement:'+#id")
    public Announcement findById(Long id) {
        return announcementDao.findOne(id);
    }

    @CacheEvict(cacheNames = "announcement", allEntries = true)
    public void deleteById(Long id) {
        announcementDao.delete(id);
    }

    @CacheEvict(cacheNames = "announcement", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            Announcement announcement = findById(id);
            Assert.notNull(announcement, "validate id!");
            deleteById(id);
        }
    }

    public int getMaxSort() {
        return announcementDao.findMaxSort();
    }

    public Page<Announcement> findAll(Predicate predicate, Pageable pageable) {
        return announcementDao.findAll(predicate, pageable);
    }

    /**
     *  * 查询最近一条公告
     *  * @author tansitao
     *  * @time 2018/11/6 11:49 
     *  
     */
//    @Cacheable(cacheNames = "announcement", key = "'entity:announcement:'+#id")
    public Announcement findLatelyAnnounce(Boolean isShow) {
        return announcementDao.findFirstByIsShowOrderBySortDesc(isShow);
    }


    @Cacheable(cacheNames = "announcement", key = "'entity:announcement:top'+'-'+#languageCode")
    public Announcement findTopAnnounce(int platform, String languageCode) {
        return announcementMapper.queryByGlobalTop(platform, languageCode);
    }

    /**
     * 查询是否存在已经全局置顶的公告
     *
     * @param isTop
     * @return true
     * @author fumy
     * @time 2018.11.19 20:41
     */
    @ReadDataSource
    public boolean isExistGlobalTop(BooleanEnum isTop) {
        int row = announcementMapper.isExistGlobalTop(isTop.getOrdinal());
        return row > 0 ? true : false;
    }
}
