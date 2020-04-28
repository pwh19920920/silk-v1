package com.spark.bitrade.envent.business;

import com.spark.bitrade.constant.CollectActionEventType;
import com.spark.bitrade.entity.transform.CollectCarrier;
import com.spark.bitrade.envent.IEnventDispatch;

/***
  * 商家审核事件分发接口
  * @author zhongxj
  * @time 2019.09.19
  */
public interface IBusinessEnventDispatch extends IEnventDispatch {
    /**
     * 事件分发
     *
     * @param carrier
     */
    default void dispatch(CollectCarrier carrier) {
        CollectActionEventType key = carrier.getCollectType();
        switch (key) {
            case BUSINESS_APPROVE:
                businessApproveEnvent(carrier);
                break;
        }
    }

    void businessApproveEnvent(CollectCarrier carrier);
}
