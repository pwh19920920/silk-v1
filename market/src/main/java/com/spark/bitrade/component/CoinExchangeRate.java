package com.spark.bitrade.component;

import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.CoinThumb;
import com.spark.bitrade.job.SyncThirdPartyPriceJob;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
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
import java.util.Objects;

/**
 * 币种汇率管理
 */
@Component
@Slf4j
@ToString
public class CoinExchangeRate {
    /**
     * USDT对cny的汇率，由SyncUsdToCnyPriceJob类完成价格的同步
     */
    @Getter
    @Setter
    private BigDecimal usdCnyRate = new BigDecimal("6.85");
    /*@Setter
    @Getter
    private BigDecimal cnyUsdRate = new BigDecimal(1/6.30);*/


    @Setter
    private CoinProcessorFactory coinProcessorFactory;

    @Autowired
    private CoinService coinService;
    @Autowired
    private ExchangeCoinService exCoinService;
    @Autowired
    private SyncThirdPartyPriceJob syncThirdPartyPriceJob;


    /**
     * 获取指定币种的USDT汇率，获取汇率的顺序 USDT、CNYT、BTC、ETH
     *
     * @param coinUint 币种简称名称，如USDT、USD、CNYT、CNY、BT、BTC、LTC...
     * @return 基于USDT转换后汇率，无汇率则返回0
     */
    public BigDecimal getUsdRate(String coinUint) {
        log.debug("getUsdRate unit = " + coinUint);

        //特殊汇率的处理
        BigDecimal specialRate = getUsdSpecialRate(coinUint);
        if (specialRate != null) {
            return specialRate;
        }

        if (coinProcessorFactory != null) {
            String usdtSymbol = getUsdtSymbol(coinUint);
            if (exCoinService.isSupported(usdtSymbol)) {
                log.debug("Support exchange coin = {}", usdtSymbol);
                return getBaseRate(usdtSymbol);
            }

            String cnytSymbol = getCnytSymbol(coinUint);
            if (exCoinService.isSupported(cnytSymbol)) {
                log.debug("Support exchange coin = {}", cnytSymbol);
                return getBaseRate(cnytSymbol);
            }

            String btSymbol = getBtSymbol(coinUint);
            if (exCoinService.isSupported(btSymbol)) {
                log.debug("Support exchange coin = {}", btSymbol);
                return getBaseRate(btSymbol);
            }

            //系统 无BTC和ETH交易区
            String btcSymbol = getBtcSymbol(coinUint);
            if (exCoinService.isSupported(btcSymbol)) {
                log.debug("Support exchange coin = {}", btcSymbol);
                return getBaseRate(btcSymbol);
            }

            String ethSymbol = getEthSymbol(coinUint);
            if (exCoinService.isSupported(ethSymbol)) {
                log.debug("Support exchange coin = {}", ethSymbol);
                return getBaseRate(ethSymbol);
            }

            return getDefaultUsdRate(coinUint);
        } else {
            return getDefaultUsdRate(coinUint);
        }
    }

    /**
     * 获取指定币种的USDT汇率（CNY返回为1），获取汇率的顺序 USDT、CNYT、BTC、ETH
     *
     * @param symbol 币种简称名称，如USDT、USD、CNYT、BT、BTC、LTC...
     * @return 基于USDT转换后汇率，无汇率则返回0
     */
    public BigDecimal getCnyRate(String symbol) {
        if (symbol.equalsIgnoreCase("CNY")) {
            return BigDecimal.ONE;
        }
        return getUsdRate(symbol).multiply(usdCnyRate).setScale(2, RoundingMode.DOWN);
    }

    /**
     * 获取基于cnyt的汇率，获取汇率的顺序 BT|--CNYT、USDT、BTC、ETH
     *
     * @param coinUint 币种简称名称，如USDT、USD、CNYT、CNY、BT、BTC、LTC...
     * @return 基于CNYT转换后汇率，无汇率则返回0
     */
    public BigDecimal getCnytRate(String coinUint) {
        //edit by young 时间： 2019.06.28 原因：下CNYT交易区，为了兼容汇率接口 将改为BT交易区
        log.debug("getCnytRate unit = " + coinUint);

        //特殊汇率的处理
        BigDecimal specialRate = getCnytSpecialRate(coinUint);
        if (specialRate != null) {
            return specialRate;
        }

        if (coinProcessorFactory != null) {
            String cnytSymbol = getCnytSymbol(coinUint);
            if (exCoinService.isSupported(cnytSymbol)) {
                log.debug("Support exchange coin = {}", cnytSymbol);
                return getCnytBaseRate(cnytSymbol);
            }

            String btSymbol = getBtSymbol(coinUint);
            if (exCoinService.isSupported(btSymbol)) {
                log.debug("Support exchange coin = {}", btSymbol);
                return getCnytBaseRate(btSymbol);
            }

            String usdtSymbol = getUsdtSymbol(coinUint);
            if (exCoinService.isSupported(usdtSymbol)) {
                log.debug("Support exchange coin = {}", usdtSymbol);
                return getCnytBaseRate(usdtSymbol);
            }

            //系统 无BTC和ETH交易区
            String btcSymbol = getBtcSymbol(coinUint);
            if (exCoinService.isSupported(btcSymbol)) {
                log.debug("Support exchange coin = {}", btcSymbol);
                return getCnytBaseRate(btcSymbol);
            }

            String ethSymbol = getEthSymbol(coinUint);
            if (exCoinService.isSupported(ethSymbol)) {
                log.debug("Support exchange coin = {}", ethSymbol);
                return getCnytBaseRate(ethSymbol);
            }

            return getDefaultCnytRate(coinUint);
        } else {
            return getDefaultCnytRate(coinUint);
        }
    }

    /**
     * 获取特殊币种基于USDT的汇率
     *
     * @param coinUint 币种简称，如USDT等
     * @return
     */
    public BigDecimal getUsdSpecialRate(String coinUint) {
        if ("USDT".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = USDT  ,result = ONE");
            return BigDecimal.ONE;
        } else if ("USD".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = USD  ,result = ONE");
            return BigDecimal.ONE;
        } else if ("CNY".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = CNY  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, 4, BigDecimal.ROUND_DOWN);
        } else if ("CNYT".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = CNYT  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, 16, BigDecimal.ROUND_UP);
        } else if ("BT".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = BT  ,result : 1 divide {}", this.usdCnyRate);
            return BigDecimal.ONE.divide(usdCnyRate, 16, BigDecimal.ROUND_UP);
        }
        // 从缓存中获取汇率价格
        return getThirdPartyUsdPrice(coinUint);
    }

    /**
     * 获取特殊币种基于USDT的汇率
     *
     * @param coinUint 币种简称，如USDT、CNYT等
     * @return
     */
    public BigDecimal getCnytSpecialRate(String coinUint) {
        if ("BT".equalsIgnoreCase(coinUint)
                || "CNY".equalsIgnoreCase(coinUint)
                || "CNYT".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = {}  ,result = ONE", coinUint);
            return BigDecimal.ONE;
        } else if ("USDT".equalsIgnoreCase(coinUint)
                || "USD".equalsIgnoreCase(coinUint)) {
            log.info("getUsdRate unit = {}  ,result :  {}", coinUint, this.usdCnyRate);
            return usdCnyRate.setScale(4, BigDecimal.ROUND_DOWN);
        }

        // 从缓存中获取汇率价格
        BigDecimal price = getThirdPartyUsdPrice(coinUint);

        if (Objects.isNull(price)) {
            return null;
        }

        return price.multiply(usdCnyRate).setScale(2, RoundingMode.DOWN);
    }

    /**
     * 获取基于交易对的价格
     *
     * @param symbol 交易对,如：BTC/USDT、BTC/CNYT
     * @return 无匹配汇率，则返回0
     */
    public BigDecimal getBaseRate(String symbol) {
        if (coinProcessorFactory != null) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(symbol);
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

        if (symbol != null && symbol.endsWith("/USDT")) {
            return getDefaultUsdRate(symbol.replace("/USDT", ""));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 转换为基于CNYT的汇率
     *
     * @param symbol 交易对,如：BTC/USDT、BTC/CNYT
     * @return 无匹配汇率，则返回0
     */
    public BigDecimal getCnytBaseRate(String symbol) {
        return getBaseRate(symbol).multiply(this.usdCnyRate).setScale(10, BigDecimal.ROUND_DOWN);
    }

    /**
     * 获取币种设置里的默认价格
     *
     * @param symbol 币种简称名称，如BTC、SLU
     * @return 查询不到默认的配置，则返回0
     */
    public BigDecimal getDefaultUsdRate(String symbol) {
        Coin coin = coinService.findByUnit(symbol);
        if (coin != null) {
            return new BigDecimal(coin.getUsdRate());
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取币种设置里转换为CNYT的默认价格
     *
     * @param coinUint 币种简称名称，如BTC、SLU
     * @return 查询不到默认的配置，则返回0
     */
    public BigDecimal getDefaultCnytRate(String coinUint) {
        return getDefaultUsdRate(coinUint).multiply(this.usdCnyRate).setScale(10, BigDecimal.ROUND_DOWN);
    }


    public String getEthSymbol(String coinUint) {
        return coinUint.toUpperCase() + "/ETH";
    }

    public String getBtcSymbol(String coinUint) {
        return coinUint.toUpperCase() + "/BTC";
    }

    public String getCnytSymbol(String coinUint) {
        return coinUint.toUpperCase() + "/CNYT";
    }

    public String getBtSymbol(String coinUint) {
        return coinUint.toUpperCase() + "/BT";
    }

    public String getUsdtSymbol(String coinUint) {
        return coinUint.toUpperCase() + "/USDT";
    }

    public BigDecimal getThirdPartyUsdPrice(String coinUnit) {
        return syncThirdPartyPriceJob.getThirdPartyUsdPrice(coinUnit);
    }

}
