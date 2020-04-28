package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.FincPlatStatDao;
import com.spark.bitrade.entity.FincPlatStat;
import com.spark.bitrade.mapper.dao.FincPlatStatMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
  * 货币统计service
  * @author tansitao
  * @time 2018/5/12 11:26 
  */
@Service
public class FincPlatStatService extends BaseService<FincPlatStat> {
    @Autowired
    private FincPlatStatDao fincPlatStatDao;
    @Autowired
    private FincPlatStatMapper fincPlatStatMapper;


    @Override
    public List<FincPlatStat> findAll() {
        return fincPlatStatDao.findAll();
    }

    public Page<FincPlatStat> findAll(Predicate predicate, Pageable pageable) {
        return fincPlatStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<FincPlatStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("time");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<FincPlatStat> geted = fincPlatStatDao.findAll(predicate, pageModel.getPageable());
        List<FincPlatStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }
    /**
     * 根据条件查询所有
     * @author Zhang Yanjun
     * @time 2018.12.17 16:45
     * @param startTime
     * @param endTime
     */
    public List<FincPlatStat> findAllBy(Date startTime, Date endTime){
        String start = startTime == null ? "" : DateUtil.getDate(startTime);
        String end = endTime == null ? "" : DateUtil.getDate(endTime);
        return fincPlatStatMapper.findAllBy(start, end);
    }

}
