package com.spark.bitrade.envent.coin.impl;

import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.coin.ICoinInEnventHandler;
import com.spark.bitrade.envent.coin.ICoinOutEnventHandler;
import com.spark.bitrade.envent.coin.ICoinEnventDispatch;
import com.spark.bitrade.envent.EnventHandlerUtil;
import org.springframework.stereotype.Service;

/***
 * 充提币相关事件分发处理实现类
 * @author yangch
 * @time 2018.11.02 15:31
 */
@Service
public class CoinEnventDispatchImpl implements ICoinEnventDispatch {

    @Override
    public void coinInEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ICoinInEnventHandler.class, carrier);
    }

    @Override
    public void coinOutEnvent(CollectCarrier carrier) {
        EnventHandlerUtil.callHandle(ICoinOutEnventHandler.class, carrier);
    }
}
