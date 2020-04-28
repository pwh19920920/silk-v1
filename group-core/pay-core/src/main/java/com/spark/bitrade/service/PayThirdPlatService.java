package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.dao.PayThirdPlatDao;
import com.spark.bitrade.entity.ThirdPlatform;
import com.spark.bitrade.mapper.dao.PayThirdPlatMapper;
import com.spark.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author fumy
 * @time 2018.10.23 15:05
 */
@Service
public class PayThirdPlatService extends TopBaseService<ThirdPlatform,PayThirdPlatDao> {

    @Autowired
    PayThirdPlatMapper thirdPlatMapper;
    @Autowired
    PayThirdPlatDao platDao;

    public PageInfo<ThirdPlatform> getThirdPlatList(String platName,int pageNo,int pageSize){
        Page<ThirdPlatform> page = PageHelper.startPage(pageNo,pageSize);
        thirdPlatMapper.getThirdPlayList(platName);
        return page.toPageInfo();
    }

    /**
     * 根据申请key查询第三方平台信息
     * @author fumy
     * @time 2018.10.23 17:16
     * @param applyKey
     * @return true
     */
    @Cacheable(cacheNames = "thirdPlatform", key = "'entity:thirdPlatform:'+#applyKey")
    public ThirdPlatform findByKey(String applyKey){
        return thirdPlatMapper.getByKey(applyKey);
    }

    @CacheEvict(cacheNames = "thirdPlatform", key = "'entity:thirdPlatform:'+#tpf.platformKey")
    public ThirdPlatform save(ThirdPlatform tpf){
        return platDao.save(tpf);
    }
}
