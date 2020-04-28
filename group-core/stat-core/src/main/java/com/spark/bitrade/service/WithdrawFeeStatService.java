package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.WithdrawFeeStatDao;
import com.spark.bitrade.entity.WithdrawFeeStat;
import com.spark.bitrade.mapper.dao.WithdrawFeeStatMapper;
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
 * @time 2018.06.13 16:38
 */
@Service
public class WithdrawFeeStatService extends BaseService<WithdrawFeeStat>{

    @Autowired
    private WithdrawFeeStatDao withdrawFeeStatDao;

    @Autowired
    private WithdrawFeeStatMapper withdrawFeeStatMapper;

    public Page<WithdrawFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return withdrawFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<WithdrawFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("time");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<WithdrawFeeStat> geted = withdrawFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<WithdrawFeeStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }

    /**
     * 根据条件查询所有
     * @author Zhang Yanjun
     * @time 2018.12.17 15:44
     * @param unit
     * @param startTime
     * @param endTime
     */
    public List<WithdrawFeeStat> findAllBy(String unit, Date startTime, Date endTime){
        String start = startTime == null ? "" : DateUtil.getDate(startTime);
        String end = endTime == null ? "" : DateUtil.getDate(endTime);
        return withdrawFeeStatMapper.findAllBy(unit, start, end);
    }
}
