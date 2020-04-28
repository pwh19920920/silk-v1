package com.spark.bitrade.service;

import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.constant.BettingConfigStatus;
import com.spark.bitrade.dao.JackpotDao;
import com.spark.bitrade.entity.BettingConfig;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.mapper.dao.JackpotMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 奖池service
 * @author tansitao
 * @time 2018/9/14 11:38 
 */
@Service
public class JackpotService extends BaseService{
    @Autowired
    private BettingConfigService bettingConfigService;
    @Autowired
    private JackpotDao jackpotDao;

    @Autowired
    private JackpotMapper jackpotMapper;

   /**
    * 查询奖池信息
    * @author tansitao
    * @time 2018/9/14 10:45 
    */
   @Cacheable(cacheNames = "jackpot", key = "'entity:jackpot:'+#periodId")
    public Jackpot findByPeriodId (long periodId){
        Jackpot jackpot = jackpotMapper.selectByPeriodId(periodId);
        return jackpot;
    }

    /**
      * 更改更改奖池余额
      * @author yangch
      * @time 2018.09.18 14:17 
      * @param periodId 奖期ID
     * @param id 奖池id
     * @param jackpotJalance 奖池余额
     */
    @CacheEvict(cacheNames = "jackpot", key = "'entity:jackpot:'+#periodId")
    @Transactional(rollbackFor = Exception.class)
    public int updateJackpotJalance( long periodId, long id,  BigDecimal jackpotJalance) {
        return jackpotDao.updateJackpotJalance(id, jackpotJalance);
    }

    @CacheEvict(cacheNames = "jackpot", key = "'entity:jackpot:'+#bean.periodId")
    @Transactional(rollbackFor = Exception.class)
    public Jackpot save(Jackpot bean){
        return jackpotDao.saveAndFlush(bean);
    }

    /**
     * 获取上一期的奖池，没有时返回默认的数据
     * @param periodId
     * @return
     */
    public Jackpot findLastByPeriodId (long periodId){
        BettingConfig bettingConfig = bettingConfigService.findConfigById(periodId);

        //上一期配置
        Map<String, Object> param = new HashMap<>();
        param.put("id", periodId);
        param.put("status", BettingConfigStatus.STAGE_FINISHED);
        BettingConfig prevBettingConfig =bettingConfigService.findForwardBetConfig(param);
        //上一期奖池
        Jackpot prevJackpot = null;
        if(null != prevBettingConfig) {
            prevJackpot = findByPeriodId(prevBettingConfig.getId());
        }

        if(null == prevJackpot) {
            prevJackpot = new Jackpot();
            prevJackpot.setPrizeSymbol(bettingConfig.getPrizeSymbol());
            prevJackpot.setRedpacketSymbol(bettingConfig.getRedpacketPrizeSymbol());
            prevJackpot.setJackpotBalance(BigDecimal.ZERO);
            prevJackpot.setRedpacketBalance(BigDecimal.ZERO);
        }

        return prevJackpot;
    }

    /**
     * 更新红包余额
     * @param jackpot
     */
    @WriteDataSource
    public int updateRedpacketBalance(Jackpot jackpot){
        return jackpotMapper.updateByPrimaryKeySelective(jackpot);
    }

}
