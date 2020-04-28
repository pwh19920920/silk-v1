package com.spark.bitrade.controller;

import com.spark.bitrade.component.CoinExchangeRate;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

@RestController
@RequestMapping("/exchange-rate")
public class ExchangeRateController {
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    /**
     * 获取指定币种的汇率，获取汇率的顺序 USDT、CNYT、BTC、ETH
     *
     * @param coin 币种简称名称，如USDT、USD、CNYT、CNY、BT、BTC、LTC...
     * @return 基于USDT转换后汇率，无汇率则返回0
     */
    @RequestMapping("usd/{coin}")
    public MessageResult getUsdExchangeRate(@PathVariable String coin) {
        MessageResult mr = new MessageResult(0, "success");
        BigDecimal latestPrice = coinExchangeRate.getUsdRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    /**
     * 获取指定币种的USDT汇率（CNY返回为1），获取汇率的顺序 USDT、CNYT、BTC、ETH
     *
     * @param coin 币种简称名称，如USDT、USD、CNYT、BT、BTC、LTC...
     * @return 基于USDT转换后汇率，无汇率则返回0
     */
    @RequestMapping("cny/{coin}")
    public MessageResult getCnyExchangeRate(@PathVariable String coin) {
        MessageResult mr = new MessageResult(0, "success");
        BigDecimal latestPrice = coinExchangeRate.getCnyRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    /**
     * 获取基于cnyt的汇率，获取汇率的顺序 CNYT、USDT、BTC、ETH
     *
     * @param coin 币种简称名称，如USDT、USD、CNYT、CNY、BT、BTC、LTC...
     * @return 基于CNYT转换后汇率，无汇率则返回0
     */
    @RequestMapping("cnyt/{coin}")
    public MessageRespResult<BigDecimal> getCnytExchangeRate(@PathVariable String coin) {
        return MessageRespResult.success4Data(coinExchangeRate.getCnytRate(coin));
    }

    @RequestMapping(value = "usd-cny", method = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST})
    public MessageResult getUsdCnyRate() {
        MessageResult mr = new MessageResult(0, "success");
        mr.setData(coinExchangeRate.getUsdCnyRate());
        return mr;
    }

    /**
     * 转换为目标币种的汇率(tips:返回中“转换币种”和“目标币种”的汇率的计价单位为USD)
     *
     * @param sourceCoin 必填，待转换的币种简称
     * @param targetCoin 必填，转换的目标币种简称
     * @param scale      可选，汇率精度
     * @return
     */
    @RequestMapping(value = "usd/to", method = {RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeToRate> toRate(@RequestParam("sourceCoin") String sourceCoin,
                                                    @RequestParam("targetCoin") String targetCoin, Integer scale) {
        MessageRespResult<ExchangeToRate> mr = new MessageRespResult(0, "success");
        ExchangeToRate toRate = new ExchangeToRate();
        toRate.setSourceCoin(sourceCoin.toUpperCase());
        toRate.setSourceCoinRate(coinExchangeRate.getUsdRate(toRate.getSourceCoin()));
        toRate.setTargetCoin(targetCoin.toUpperCase());
        toRate.setTargetCoinRate(coinExchangeRate.getUsdRate(toRate.getTargetCoin()));

        if (toRate.getSourceCoinRate().compareTo(BigDecimal.ZERO) > 0
                && toRate.getTargetCoinRate().compareTo(BigDecimal.ZERO) > 0) {

            scale = Objects.isNull(scale) || scale < 0 ? 16 : scale;

            // 转换为目标币种的汇率
            toRate.setRate(toRate.getSourceCoinRate().divide(toRate.getTargetCoinRate(), scale, BigDecimal.ROUND_DOWN));
        }

        mr.setData(toRate);
        return mr;
    }

    @Data
    private static class ExchangeToRate {
        /**
         * 汇率。tips：转换后的汇率，计价单位为目标币种
         */
        private BigDecimal rate = BigDecimal.ZERO;

        /**
         * 转换币种
         */
        private String sourceCoin;

        /**
         * 转换币种汇率。tips：计价单位由接口提供者决定，一般为CNY、USD
         */
        private BigDecimal sourceCoinRate = BigDecimal.ZERO;

        /**
         * 目标币种
         */
        private String targetCoin;

        /**
         * 目标币种汇率。tips：计价单位由接口提供者决定，一般为CNY、USD
         */
        private BigDecimal targetCoinRate = BigDecimal.ZERO;
    }
}
