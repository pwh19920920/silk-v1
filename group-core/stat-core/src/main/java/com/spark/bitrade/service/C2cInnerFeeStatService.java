package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.C2cInnerFeeStatDao;
import com.spark.bitrade.dao.ExchangeFeeStatDao;
import com.spark.bitrade.entity.C2cInnerFeeStat;
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
public class C2cInnerFeeStatService extends BaseService<C2cInnerFeeStat>{

    @Autowired
    private C2cInnerFeeStatDao c2cInnerFeeStatDao;

    public Page<C2cInnerFeeStat> findAll(Predicate predicate, Pageable pageable) {
        return c2cInnerFeeStatDao.findAll(predicate, pageable);
    }

    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<C2cInnerFeeStat> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("createTime");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.DESC);
        pageModel.setDirection(directionList);
        Iterable<C2cInnerFeeStat> geted = c2cInnerFeeStatDao.findAll(predicate, pageModel.getPageable());
        List<C2cInnerFeeStat> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        //edit by zyj
        for (int i=0;i<list.size();i++){
            list.get(i).setTypeOut(list.get(i).getType()==0?"买":"卖");//交易类型
        }
        return list;
    }
}
