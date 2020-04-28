package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.CollectLogDao;
import com.spark.bitrade.entity.CollectionLog;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
  * 归集明细service
  * @author tansitao
  * @time 2018/5/12 11:26 
  */
@Service
public class CollectLogService extends BaseService<CollectionLog> {
    @Autowired
    private CollectLogDao collectLogDao;


    @Override
    public List<CollectionLog> findAll() {
        return collectLogDao.findAll();
    }

    public Page<CollectionLog> findAll(Predicate predicate, Pageable pageable) {
        return collectLogDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<CollectionLog> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<CollectionLog> geted = collectLogDao.findAll(predicate, pageModel.getPageable());
        List<CollectionLog> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }
}
