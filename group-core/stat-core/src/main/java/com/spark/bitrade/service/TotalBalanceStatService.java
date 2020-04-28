package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.entity.TotalBalanceStat;
import com.spark.bitrade.mapper.dao.TotalBalanceStatMapper;
import com.spark.bitrade.vo.TotalBalanceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fumy
 * @time 2018.09.27 19:29
 */
@Service
public class TotalBalanceStatService {

    @Autowired
    TotalBalanceStatMapper mapper;

    @ReadDataSource
    public PageInfo<TotalBalanceStat> getDayOfList(String unit,int pageNo,int pageSize){
        Page<TotalBalanceStat> page= PageHelper.startPage(pageNo,pageSize);
        mapper.getDayOfTotal(unit);
        return page.toPageInfo();
    }

    //得到日统计中的钱包余额
    @ReadDataSource
    public TotalBalanceVo getDayOfWalletBalance(String unit, String opDate){

        return mapper.getWalletBalance(unit,opDate);
    }

}
