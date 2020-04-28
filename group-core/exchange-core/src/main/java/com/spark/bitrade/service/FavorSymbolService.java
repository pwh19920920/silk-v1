package com.spark.bitrade.service;


import com.spark.bitrade.dao.FavorSymbolRepository;
import com.spark.bitrade.entity.FavorSymbol;
import com.spark.bitrade.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class FavorSymbolService {
    @Autowired
    private FavorSymbolRepository favorSymbolRepository;

    /**
     * 添加自选
     * @param memberId
     * @param symbol
     * @return
     */
    @CacheEvict(cacheNames = "favorSymbol", allEntries = true)
    public FavorSymbol add(Long memberId,String symbol){
        FavorSymbol favor = new FavorSymbol();
        favor.setMemberId(memberId);
        favor.setAddTime(DateUtil.getDateTime());
        favor.setSymbol(symbol);
        return favorSymbolRepository.save(favor);
    }

    /**
     * 删除自选
     * @param memberId
     * @param symbol
     */
    @CacheEvict(cacheNames = "favorSymbol", key = "'entity:favorSymbol:'+#memberId")
    public void delete(Long memberId,String symbol){
        List<FavorSymbol> favor = favorSymbolRepository.findByMemberIdAndSymbol(memberId,symbol);
        if(!CollectionUtils.isEmpty(favor)){
            favorSymbolRepository.delete(favor);
        }
    }

    /**
     * 查询会员所有的自选
     * @param memberId
     * @return
     */
    @Cacheable(cacheNames = "favorSymbol", key = "'entity:favorSymbol:'+#memberId")
    public List<FavorSymbol> findByMemberId(Long memberId){
        return favorSymbolRepository.findAllByMemberId(memberId);
    }

    /**
     * 查询某个自选
     * @param memberId
     * @param symbol
     * @return
     */
    public List<FavorSymbol> findByMemberIdAndSymbol(Long memberId, String symbol){
        return favorSymbolRepository.findByMemberIdAndSymbol(memberId,symbol);
    }
}
