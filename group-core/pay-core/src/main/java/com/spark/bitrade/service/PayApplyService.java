package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.PayApplyDao;
import com.spark.bitrade.entity.ThirdPlatformApply;
import com.spark.bitrade.mapper.dao.PayApplyMapper;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @author fumy
 * @time 2018.10.23 14:48
 */
@Service
public class PayApplyService extends TopBaseService<ThirdPlatformApply,PayApplyDao>{

    @Autowired
    PayApplyMapper payApplyMapper;
    @Autowired
    PayApplyDao payApplyDao;


    @ReadDataSource
    public PageInfo<ThirdPlatformApply> findByPgae(String bussiAcccount,Integer status,int pageNo,int pageSize){
        Page<ThirdPlatformApply> page = PageHelper.startPage(pageNo,pageSize);
        payApplyMapper.getApplyList(bussiAcccount,status);
        return page.toPageInfo();
    }


    @CacheEvict(cacheNames = "thirdPlatformApply", key = "'entity:thirdPlatformApply:'+#busiAccount")
    public ThirdPlatformApply save(ThirdPlatformApply tpa){
        return payApplyDao.save(tpa);
    }

    public ThirdPlatformApply findById(Long id){
        return payApplyDao.findOne(id);
    }
}
