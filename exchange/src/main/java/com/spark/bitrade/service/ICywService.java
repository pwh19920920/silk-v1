package com.spark.bitrade.service;

import com.spark.bitrade.entity.ExchangeOrder;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *  机器人查询订单服务接口
 *
 * @author young
 * @time 2019.09.06 18:01
 */
public interface ICywService {

    /**
     * 查询正在交易的订单
     *
     * @param symbol   交易对，eg：SLU/USDT
     * @return
     */
    List<ExchangeOrder> openOrders(@RequestParam("symbol") String symbol);

}
