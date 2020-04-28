package com.spark.bitrade.dao;

import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.dao.base.BaseDao;
import com.spark.bitrade.entity.CustomerLockCoinDetail;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.UnlockCoinDetail;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
  * 增加锁仓详细
  * @author tansitao
  * @time 2018/6/12 16:53 
  */
public interface UnLockCoinDetailDao extends BaseDao<UnlockCoinDetail> {

}
