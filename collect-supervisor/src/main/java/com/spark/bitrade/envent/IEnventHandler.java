package com.spark.bitrade.envent;

import com.spark.bitrade.entity.transform.CollectCarrier;

/***
 * 事件处理接口
 *
 * @author yangch
 * @time 2018.11.02 14:18
 */
public interface IEnventHandler {
    /**
     * 事件处理的顺序
     * @return
     */
    default int enventOrder(){
        return 0;
    }

    /**
     * 事件处理方法
     * @param carrier
     */
    void handle(CollectCarrier carrier);
}
