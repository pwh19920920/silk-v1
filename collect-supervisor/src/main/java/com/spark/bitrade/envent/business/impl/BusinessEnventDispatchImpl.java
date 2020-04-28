package com.spark.bitrade.envent.business.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.EnventHandlerUtil;
import com.spark.bitrade.envent.business.IBusinessApprovedEnventHandler;
import com.spark.bitrade.envent.business.IBusinessEnventDispatch;
import org.springframework.stereotype.Service;

/***
  * 商家审核事件分发处理实现类
  * @author yangch
  * @time 2018.11.02 14:02
  */
@Service
public class BusinessEnventDispatchImpl implements IBusinessEnventDispatch {

    @Override
    public void businessApproveEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IBusinessApprovedEnventHandler.class, carrier);
    }
}
