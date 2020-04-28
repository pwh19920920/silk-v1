package com.spark.bitrade.envent.customizing;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;

/***
  * 运营手动编辑站内信
  * @author zhongxj
  * @time 2019.09.29
  */
public interface ICustomizingEnventDispatch extends IEnventDispatch {
    /**
     * 事件分发
     *
     * @param carrier
     */
    default void dispatch(CollectCarrier carrier) {
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case MANUAL_INSTATION:
                customizingEnvent(carrier);
                break;
        }
    }

    void customizingEnvent(CollectCarrier carrier);
}
