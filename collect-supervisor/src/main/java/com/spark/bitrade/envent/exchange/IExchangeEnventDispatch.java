package com.spark.bitrade.envent.exchange;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;

/***
 * 币币交易相关事件分发接口
 * @author yangch
 * @time 2018.11.02 13:57
 */
public interface IExchangeEnventDispatch extends IEnventDispatch {
    default void dispatch(CollectCarrier carrier) {
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case EXCHANGE_ADD_ORDER :
                addOrderEnvent(carrier);
                break;
            case EXCHANGE_CANCEL_ORDER :
                cancelOrderEnvent(carrier);
                break;
        }
    }

    void addOrderEnvent(CollectCarrier carrier);
    void cancelOrderEnvent(CollectCarrier carrier);
}
