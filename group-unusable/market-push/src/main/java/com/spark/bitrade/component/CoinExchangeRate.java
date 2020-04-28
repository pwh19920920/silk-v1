package com.spark.bitrade.component;

import com.spark.bitrade.cache.CoinCacheProcessor;
import com.spark.bitrade.cache.CoinCacheProcessorFactory;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.service.CoinService;
import com.spark.bitrade.service.ExchangeCoinService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 币种汇率管理
 */
@Component
@Slf4j
@ToString
public class CoinExchangeRate {
    @Getter
    @Setter
    private BigDecimal usdCnyRate = new BigDecimal("6.85"); //定期更新USD的汇率(由通过消息来更新)

    @Setter
    private CoinCacheProcessorFactory coinProcessorFactory;

    @Autowired
    private CoinService coinService;
    @Autowired
    private ExchangeCoinService exCoinService;


    /**
     * 获取基于usdt的汇率
     * @param coinUint 币种
     * @return
     */
    public BigDecimal getUsdRate(String coinUint) {
        log.debug("CoinExchangeRate getUsdRate unit = " + coinUint);
        if (coinUint.equalsIgnoreCase("USDT")) {
            log.debug("CoinExchangeRate getUsdRate unit = USDT  ,result = ONE");
            return BigDecimal.ONE;
        } else if (coinUint.equalsIgnoreCase("CNY")) {
            log.debug("CoinExchangeRate getUsdRate unit = CNY  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, BigDecimal.ROUND_DOWN).setScale(4, BigDecimal.ROUND_DOWN);
        } else if (coinUint.equalsIgnoreCase("CNYT")) {
            log.debug("CoinExchangeRate getUsdRate unit = CNYT  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, BigDecimal.ROUND_UP).setScale(4, BigDecimal.ROUND_UP);
        }

        if (coinProcessorFactory != null) {
            String usdtSymbol = coinUint.toUpperCase() + "/USDT";
            if (exCoinService.isSupported(usdtSymbol)) {
                log.debug("Support exchange coin = {}", usdtSymbol);
                return getBaseRate(usdtSymbol);
            }

            //注：实际情况很难进入该段代码
            String btcSymbol = coinUint.toUpperCase() + "/BTC";
            String ethSymbol = coinUint.toUpperCase() + "/ETH";
            if (exCoinService.isSupported(ethSymbol)) {
                log.debug("Support exchange coin = {}", ethSymbol);
                return getBaseRate(ethSymbol);
            } else if (exCoinService.isSupported(btcSymbol)) {
                log.debug("Support exchange coin = {}", btcSymbol);
                return getBaseRate(btcSymbol);
            } else {
                return getDefaultUsdRate(coinUint);
            }
        } else {
            return getDefaultUsdRate(coinUint);
        }
    }

    /**
     * 获取基于交易对的汇率
     *
     * @param symbol 交易对
     * @return
     */
    public BigDecimal getBaseRate(String symbol){
        if (coinProcessorFactory != null) {
            CoinCacheProcessor processor = coinProcessorFactory.getProcessor(symbol);
            if (null != processor) {
                CoinThumb thumb = processor.getThumb();
                if (null != thumb) {
                    return thumb.getUsdRate();
                } else {
                    log.warn("行情为null，symbol={}", symbol);
                }
            } else {
                log.warn("交易对不存在，symbol={}", symbol);
            }
        }

        if(symbol!=null && symbol.endsWith("/USDT")){
            return getDefaultUsdRate(symbol.replace("/USDT",""));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取币种设置里的默认价格
     *
     * @param symbol
     * @return
     */
    public BigDecimal getDefaultUsdRate(String symbol) {
        Coin coin = coinService.findByUnit(symbol);
        if (coin != null) {
            return new BigDecimal(coin.getUsdRate());
        } else return BigDecimal.ZERO;
    }

    public BigDecimal getCnyRate(String symbol) {
        if (symbol.equalsIgnoreCase("CNY")) {
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdCnyRate).setScale(2, RoundingMode.DOWN);
    }
}
