package com.spark.bitrade.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.entity.ExchangeFastAccountDO;
import com.spark.bitrade.service.IExchangeFastAccountService;
import com.spark.bitrade.mapper.dao.ExchangeFastAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class ExchangeFastAccountServiceImpl
        extends ServiceImpl<ExchangeFastAccountMapper, ExchangeFastAccountDO>
        implements IExchangeFastAccountService {


    /**
     * 根据币种和应用ID获取闪兑总账户接口
     *
     * @param appId      必填，应用ID
     * @param coinSymbol 必填，闪兑币种，如BTC、LTC
     * @param baseSymbol 闪兑基币
     * @return
     */
    @Override
    @Cacheable(cacheNames = "exchangeFastAccount", key = "'entity:exchangeFastAccount:'+#appId+'-'+#coinSymbol+'-'+#baseSymbol")
    public ExchangeFastAccountDO findByAppIdAndCoinSymbol(String appId, String coinSymbol, String baseSymbol) {
        List<ExchangeFastAccountDO> lst = this.baseMapper.findByAppIdAndCoinSymbol(appId, coinSymbol, baseSymbol);

        if (lst.size() > 1) {
            //随机获取一个账户
            Random random = new Random();
            int randomInt = random.nextInt(lst.size());
            if (randomInt < 0 || randomInt > lst.size()) {
                log.warn("随机获取一个账户异常：randomInt={},size={}", randomInt, lst.size());
                randomInt = 0;
            }

            return lst.get(randomInt);
        } else if (lst.size() == 1) {
            return lst.get(0);
        }

        return null;
    }

}
