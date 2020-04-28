package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.C2cFeeStatDao;
import com.spark.bitrade.dao.ExchangeFeeStatDao;
import com.spark.bitrade.entity.C2cFeeStat;
import com.spark.bitrade.entity.ExchangeFeeStat;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shenzucai
 * @time 2018.06.20 15:39
 */
@Service
public class C2cFeeStatService extends BaseService<C2cFeeStat> {

    @Autowired
    private C2cFeeStatDao c2cFeeStatDao;

    public Page<C2cFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return c2cFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<C2cFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<C2cFeeStat> geted = c2cFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<C2cFeeStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        //edit by zyj
        for (int i=0;i<list.size();i++){
            list.get(i).setTypeOut(list.get(i).getType()==0?"买":"卖");//交易类型
        }
        return list;
    }
}
