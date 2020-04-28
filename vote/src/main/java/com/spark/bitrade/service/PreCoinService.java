package com.spark.bitrade.service;

import com.spark.bitrade.dao.PreCoinDao;
import com.spark.bitrade.entity.PreCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Jinwei
 * @date 2018年03月27日
 */
@Service
public class PreCoinService {
    @Autowired
    private PreCoinDao preCoinDao;

    public PreCoin findById(Long id){
        return preCoinDao.findOne(id);
    }

    public PreCoin save(PreCoin preCoin){
        return  preCoinDao.saveAndFlush(preCoin);
    }
}
