package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Announcement;
import org.springframework.data.jpa.repository.Query;

/**
 * @author rongyu
 * @description
 * @date 2018/3/5 15:32
 */
public interface AnnouncementDao extends BaseDao<Announcement> {

    @Query("select max(s.sort) from Announcement s")
    int findMaxSort();

    /**
     * 查询最新的公告
     * @author tansitao
     * @time 2018/11/7 11:15 
     */
    Announcement findFirstByIsShowOrderBySortDesc(Boolean isShow);
}
