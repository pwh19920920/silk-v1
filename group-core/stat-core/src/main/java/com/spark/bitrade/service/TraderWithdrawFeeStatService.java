package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.TraderWithdrawFeeStatDao;
import com.spark.bitrade.entity.TraderWithdrawFeeStat;
import com.spark.bitrade.mapper.dao.TraderWithdrawFeeStatMapper;
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
public class TraderWithdrawFeeStatService extends BaseService<TraderWithdrawFeeStat>{

    @Autowired
    private TraderWithdrawFeeStatDao traderWithdrawFeeStatDao;

    @Autowired
    private TraderWithdrawFeeStatMapper traderWithdrawFeeStatMapper;

    public Page<TraderWithdrawFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return traderWithdrawFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<TraderWithdrawFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("time");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<TraderWithdrawFeeStat> geted = traderWithdrawFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<TraderWithdrawFeeStat> list = Lists.newArrayList();
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
    public List<TraderWithdrawFeeStat> findAllBy(String unit, Date startTime, Date endTime){
        String start = startTime == null ? "" : DateUtil.getDate(startTime);
        String end = endTime == null ? "" : DateUtil.getDate(endTime);
        return traderWithdrawFeeStatMapper.findAllBy(unit, start, end);
    }
}
