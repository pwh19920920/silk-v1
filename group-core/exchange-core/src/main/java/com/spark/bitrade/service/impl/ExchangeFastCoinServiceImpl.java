package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.entity.ExchangeFastCoinDO;
import com.spark.bitrade.service.IExchangeFastCoinService;
import com.spark.bitrade.mapper.dao.ExchangeFastCoinMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ExchangeFastCoinServiceImpl
        extends ServiceImpl<ExchangeFastCoinMapper, ExchangeFastCoinDO>
        implements IExchangeFastCoinService {

    @Override
    @WriteDataSource
    public int save(ExchangeFastCoinDO fastCoinDO) {
        fastCoinDO.setId(fastCoinDO.getBaseSymbol() + fastCoinDO.getCoinSymbol() + fastCoinDO.getAppId() + "");
        return this.baseMapper.insert(fastCoinDO);
    }

    @Override
    @Cacheable(cacheNames = "exchangeFastCoin", key = "'entity:exchangeFastCoin:'+#appId+'-'+#coinSymbol")
    public ExchangeFastCoinDO findByAppIdAndCoinSymbol(String appId, String coinSymbol) {
        return this.baseMapper.findByAppIdAndCoinSymbol(appId, coinSymbol, null);
    }

    @Override
    @Cacheable(cacheNames = "exchangeFastCoin", key = "'entity:exchangeFastCoin:'+#appId+'-'+#coinSymbol+'-'+#baseSymbol")
    public ExchangeFastCoinDO findByAppIdAndCoinSymbol(String appId, String coinSymbol, String baseSymbol) {
        return this.baseMapper.findByAppIdAndCoinSymbol(appId, coinSymbol, baseSymbol);
    }

    @Override
    @Cacheable(cacheNames = "exchangeFastCoin", key = "'entity:exchangeFastCoin:list4Coin-'+#appId+'-'+#baseSymbol")
    public List<ExchangeFastCoinDO> list4CoinSymbol(String appId, String baseSymbol) {
        return this.baseMapper.list4CoinSymbol(appId, baseSymbol);
    }

    @Override
    @Cacheable(cacheNames = "exchangeFastCoin", key = "'entity:exchangeFastCoin:list4Base-'+#appId")
    public List<String> list4BaseSymbol(String appId) {
        return this.baseMapper.list4BaseSymbol(appId);
    }

    @Override
    public String getRateValidBaseSymbol(ExchangeFastCoinDO exchangeFastCoin) {
        return StringUtils.isEmpty(exchangeFastCoin.getRateReferenceBaseSymbol())
                ? exchangeFastCoin.getBaseSymbol() : exchangeFastCoin.getRateReferenceBaseSymbol();
    }

    @Override
    public String getRateValidCoinSymbol(ExchangeFastCoinDO exchangeFastCoin) {
        return StringUtils.isEmpty(exchangeFastCoin.getRateReferenceCoinSymbol())
                ? exchangeFastCoin.getCoinSymbol() : exchangeFastCoin.getRateReferenceCoinSymbol();
    }


}
