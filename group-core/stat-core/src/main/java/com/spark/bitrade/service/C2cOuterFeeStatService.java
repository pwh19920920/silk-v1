package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.C2cInnerFeeStatDao;
import com.spark.bitrade.dao.C2cOuterFeeStatDao;
import com.spark.bitrade.entity.C2cInnerFeeStat;
import com.spark.bitrade.entity.C2cOuterFeeStat;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author fumy
* @time 2018.09.26 17:38
*/
@Service
public class C2cOuterFeeStatService extends BaseService<C2cOuterFeeStat>{

    @Autowired
    private C2cOuterFeeStatDao c2cOuterFeeStatDao;

    public Page<C2cOuterFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return c2cOuterFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author fumy
     * @time 2018.09.26 17:39
     * @param predicate
     * @return true
     */
    public List<C2cOuterFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<C2cOuterFeeStat> geted = c2cOuterFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<C2cOuterFeeStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }
}
