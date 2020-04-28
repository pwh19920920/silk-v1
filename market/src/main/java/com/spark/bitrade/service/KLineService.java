package com.spark.bitrade.service;

import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.entity.KLine;
import com.spark.bitrade.service.base.KLineBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * K线服务接口（适配MongoDB线程池）
 *
 * @author yangch
 * @time 2018.07.16 17:28  
 */

@Slf4j
@Service
public class KLineService extends KLineBaseService {

    /**
     * 异步更新24消息交易量和成交额
     *
     * @param coinThumb
     * @param symbol
     * @param time
     * @param isInitialize 是否初始化
     */
    @Async("mongodb2")
    @Override
    public void asyncUpdate24HVolumeAndTurnover(CoinThumb coinThumb, final String symbol, final long time, final boolean isInitialize) {
        super.asyncUpdate24HVolumeAndTurnover(coinThumb, symbol, time, isInitialize);
    }


    /**
     *  保存k线(异步保存K线数据)
     *  @author yangch
     *  @time 2018.07.16 14:17 
     *
     * @param symbol 交易对名称
     * @param kLine  k线数据
     *                
     */
    @Async("mongodb2")
    @Override
    public void saveKLine(final String symbol, final KLine kLine) {
        super.saveKLine(symbol, kLine);
    }
}
