package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.dao.CoinTokenDao;
import com.spark.bitrade.entity.CoinToken;
import com.spark.bitrade.mapper.dao.CoinTokenMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.vo.CoinTokenVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author fumy
 * @time 2018.09.05 10:31
 */
@Service
public class CoinTokenService extends BaseService{

    @Autowired
    CoinTokenMapper mapper;

    @Autowired
    CoinTokenDao coinTokenDao;


    @ReadDataSource
    public PageInfo<CoinTokenVo> findByPage(Map<String,String> params, int pageNo, int pageSize){
        Page<CoinTokenVo> page = PageHelper.startPage(pageNo,pageSize);
        mapper.queryCoinTokenList(params);
        return page.toPageInfo();
    }

    /**
     * 查询所有带币
     * @author tansitao
     * @time 2018/9/7 14:46 
     */
    @ReadDataSource
    @Cacheable(cacheNames = "coinTokenVo" , key = "'entity:coinTokenVo:all'")
    public List findAll(){
        return  mapper.queryCoinTokenList(null);
    }



    @CacheEvict(cacheNames = "coinTokenVo", allEntries = true)//add by tansitao 时间： 2018/9/7 原因：清空缓存
//    @WriteDataSource
    public CoinToken updateById(CoinToken coinToken){
//        int row = mapper.updateById(coinTokenVo);
//        return row > 0 ? true : false;
        return coinTokenDao.save(coinToken);
    }

    @CacheEvict(cacheNames = "coinTokenVo", allEntries = true)//add by tansitao 时间： 2018/9/7 原因：清空缓存
//    @WriteDataSource
    public CoinToken insertNew(CoinToken coinToken){
//        int row = mapper.insertNew(coinTokenVo);
//        return row > 0 ? true : false;
        return coinTokenDao.save(coinToken);
    }

    @CacheEvict(cacheNames = "coinTokenVo", allEntries = true)//add by tansitao 时间： 2018/9/7 原因：清空缓存
//    @WriteDataSource
    public void deleteById(Long id){
        coinTokenDao.delete(id);
//        int row = mapper.deleteById(id);
//        return row > 0 ? true : false;
    }

}
