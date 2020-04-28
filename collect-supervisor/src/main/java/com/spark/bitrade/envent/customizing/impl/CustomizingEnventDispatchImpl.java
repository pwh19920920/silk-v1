package com.spark.bitrade.envent.customizing.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.EnventHandlerUtil;
import com.spark.bitrade.envent.customizing.ICustomizingEnventDispatch;
import com.spark.bitrade.envent.customizing.ICustomizingEnventHandler;
import org.springframework.stereotype.Service;

/***
  * 运营手动编辑站内信
  * @author zhongxj
  * @time 2019.09.29
  */
@Service
public class CustomizingEnventDispatchImpl implements ICustomizingEnventDispatch {

    @Override
    public void customizingEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ICustomizingEnventHandler.class, carrier);
    }
}
