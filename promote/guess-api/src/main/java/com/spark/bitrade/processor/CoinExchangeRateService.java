package com.spark.bitrade.processor;

import com.spark.bitrade.entity.CoinThumb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/***
 * 汇率转化接口
 * @author yangch
 * @time 2018.09.19 17:09
 */
@Service
public class CoinExchangeRateService {
    @Autowired
    private ICoinExchangeRate iCoinExchangeRate;


    /**
     * 获取币种的汇率
     * @param symbol
     * @return
     */
    public Optional<CoinThumb> findSymbolThumb(final String symbol){
        List<CoinThumb> lst = iCoinExchangeRate.findSymbolThumb(null, symbol);
        if(null == lst || null == symbol){
            return Optional.empty();
        }

        return lst.stream().filter(coinThumb -> coinThumb.getSymbol().equalsIgnoreCase(symbol)).findFirst();
    }

    /**
     * 获取币种基于USDT的汇率
     * @param coinUnitName 币种的简写名称
     * @return
     */
    public Optional<CoinThumb> findSymbolThumb4USDT(String coinUnitName){
        if(null != coinUnitName) {
            String symbol = coinUnitName+"/USDT";
            return findSymbolThumb(symbol);
        }

        return Optional.empty();
    }

    /**
     * 币种转化汇率
     * @param sourceCoinUnit 转换前的币种简写名称
     * @param targetCoinUnit 转换后的币种简写名称
     * @return 汇率
     */
    public BigDecimal toRate(String sourceCoinUnit, String targetCoinUnit){
        if(targetCoinUnit.equalsIgnoreCase(sourceCoinUnit)){
            return BigDecimal.ONE;
        }

        Optional<CoinThumb> source = findSymbolThumb4USDT(sourceCoinUnit);
        Optional<CoinThumb> target = findSymbolThumb4USDT(targetCoinUnit);
        if(source.isPresent() && target.isPresent()) {
            if(target.get().getClose().compareTo(BigDecimal.ZERO) == 0){
                return BigDecimal.ZERO;
            } else {
                return source.get().getClose().divide(target.get().getClose(), 8, BigDecimal.ROUND_HALF_EVEN);
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * 币种汇率转化
     * @param sourceAmount 转化前的数目
     * @param sourceCoinUnit 转换前的币种简写名称
     * @param targetCoinUnit 转换后的币种简写名称
     * @return 转换后的数目
     */
    public BigDecimal toRate(BigDecimal sourceAmount,
                             String sourceCoinUnit, String targetCoinUnit) {
        return toRate(sourceCoinUnit, targetCoinUnit).multiply(sourceAmount).setScale(8, BigDecimal.ROUND_HALF_EVEN);
    }
}
