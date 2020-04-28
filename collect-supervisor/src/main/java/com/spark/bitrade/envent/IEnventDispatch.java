package com.spark.bitrade.envent;

import com.spark.bitrade.entity.transform.CollectCarrier;

/***
  * 事件分发接口
  * @author yangch
  * @time 2018.11.02 13:57
  */
public interface IEnventDispatch {
    /**
     * 事件分发
     *
     * @param carrier
     */
    void dispatch(CollectCarrier carrier);
}
