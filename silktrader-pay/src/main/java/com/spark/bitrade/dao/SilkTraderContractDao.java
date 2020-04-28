package com.spark.bitrade.dao;

import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.SilkTraderContract;

/**
 * @author shenzucai
 * @time 2018.07.02 16:02
 */
public interface SilkTraderContractDao extends BaseDao<SilkTraderContract>{

    public SilkTraderContract queryAllById(Long id);
}
