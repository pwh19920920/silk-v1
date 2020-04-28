package com.spark.bitrade.envent.exchange.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.exchange.ICancelOrderEnventHandler;
import com.spark.bitrade.envent.exchange.IAddOrderEnventHandler;
import com.spark.bitrade.envent.exchange.IExchangeEnventDispatch;
import com.spark.bitrade.envent.EnventHandlerUtil;
import org.springframework.stereotype.Service;

/***
 * 币币交易相关事件分发处理实现类
 * @author yangch
 * @time 2018.11.02 15:34
 */
@Service
public class ExchangeEnventDispatchImpl implements IExchangeEnventDispatch {

    @Override
    public void addOrderEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(IAddOrderEnventHandler.class, carrier);
    }

    @Override
    public void cancelOrderEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ICancelOrderEnventHandler.class, carrier);
    }
}
