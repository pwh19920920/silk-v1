package com.spark.bitrade.envent.coin;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;

/***
  * 充提币相关事件分发接口
  * @author yangch
  * @time 2018.11.02 13:57
  */
public interface ICoinEnventDispatch extends IEnventDispatch {
    /**
     * 事件分发
     *
     * @param carrier
     */
    default void dispatch(CollectCarrier carrier) {
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case COIN_IN:
                coinInEnvent(carrier);
                break;
            case COIN_OUT:
                coinOutEnvent(carrier);
                break;
        }
    }

    void coinInEnvent(CollectCarrier carrier);

    void coinOutEnvent(CollectCarrier carrier);
}
