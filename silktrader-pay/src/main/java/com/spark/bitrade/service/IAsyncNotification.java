package com.spark.bitrade.service;

/**
 * @author shenzucai
 * @time 2019.07.29 13:49
 */
public interface IAsyncNotification {

    /**
     *
     * @author shenzucai
     * @time 2019.07.29 13:52
     * @param appid 应用渠道表示
     * @param orderId 种子商城订单号id
     * @param tradeSn 云端转账交易编号
     * @param status 转账结果，成功为ok，错误为出错说明
     * @param tag 转账标志
     * @return true
     */
    void asyncNotification(String appid,String orderId,String tradeSn,String status,String tag);
}
