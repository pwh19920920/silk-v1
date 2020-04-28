package com.spark.bitrade.envent.otc.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.EnventHandlerUtil;
import com.spark.bitrade.envent.otc.*;
import org.springframework.stereotype.Service;

/***
  * C2C相关事件分发处理实现类
  * @author yangch
  * @time 2018.11.02 14:02
  */
@Service
public class OtcEnventDispatchImpl implements IOtcEnventDispatch {

    @Override
    public void addOrderEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IAddOrderEnventHandler.class, carrier);
    }

    @Override
    public void appealOrderEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IAppealOrderEnventHandler.class, carrier);
    }

    @Override
    public void cancelOrderEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ICancelOrderEnventHandler.class, carrier);
    }

    @Override
    public void payCashEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IPayCashEnventHandler.class, carrier);
    }

    @Override
    public void payCoinEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IPayCoinEnventHandler.class, carrier);
    }

    @Override
    public void expireRemindOrder(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IExpireRemindOrderEnventHandler.class, carrier);
    }


    @Override
    public void appealOrderFinished(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IAppealOrderCompletedEnventHandler.class, carrier);
    }
}
