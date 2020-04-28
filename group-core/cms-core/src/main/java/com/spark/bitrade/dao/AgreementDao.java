package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.Agreement;
import com.spark.bitrade.entity.Announcement;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 增加协议dao
 * @author tansitao
 * @time 2018/4/25 14:37 
 */
public interface AgreementDao extends BaseDao<Agreement> {

    @Query("select max(s.sort) from Agreement s")
    int findMaxSort();

    List<Agreement> findAllByisShow(boolean isShow);
}
