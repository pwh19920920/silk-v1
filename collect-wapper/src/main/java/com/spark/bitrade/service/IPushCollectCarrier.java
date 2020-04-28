package com.spark.bitrade.service;

import com.spark.bitrade.entity.transform.CollectCarrier;

/***
  * 
  * @author yangch
  * @time 2018.11.01 19:32
  */
public interface IPushCollectCarrier {
    /**
     * 推送
     *
     * @param carrier 事件参数
     */
    void push(CollectCarrier carrier);
}
