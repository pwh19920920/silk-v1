package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.ExchangeFeeStatDao;
import com.spark.bitrade.entity.ExchangeFeeStat;
import com.spark.bitrade.mapper.dao.ExchangeFeeStatMapper;
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
 * @author shenzucai
 * @time 2018.06.19 19:37
 */
@Service
public class ExchangeFeeStatService extends BaseService<ExchangeFeeStat>{

    @Autowired
    private ExchangeFeeStatDao exchangeFeeStatDao;
    @Autowired
    private ExchangeFeeStatMapper exchangeFeeStatMapper;

    public Page<ExchangeFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return exchangeFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<ExchangeFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<ExchangeFeeStat> geted = exchangeFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<ExchangeFeeStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }

    /**
     * 根据条件查询所有
     * @author Zhang Yanjun
     * @time 2018.12.17 16:45
     * @param baseUnit
     * @param coinUnit
     * @param startTime
     * @param endTime
     */
    public List<ExchangeFeeStat> findAllBy(String baseUnit, String coinUnit, Date startTime, Date endTime){
        String start = startTime == null ? "" : DateUtil.getDate(startTime);
        String end = endTime == null ? "" : DateUtil.getDate(endTime);
        return exchangeFeeStatMapper.findAllBy(baseUnit, coinUnit, start, end);
    }
}
