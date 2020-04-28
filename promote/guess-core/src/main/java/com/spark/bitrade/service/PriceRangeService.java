package com.spark.bitrade.service;

import com.spark.bitrade.dao.BettingPriceRangeDao;
import com.spark.bitrade.entity.BettingPriceRange;
import com.spark.bitrade.entity.Jackpot;
import com.spark.bitrade.mapper.dao.BettingPriceRangeMapper;
import com.spark.bitrade.mapper.dao.JackpotMapper;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.SpringContextUtil;
import com.spark.bitrade.vo.PriceRangeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 投票价格区间service
 * @author tansitao
 * @time 2018/9/14 11:39 
 */
@Service
public class PriceRangeService extends BaseService{
    @Autowired
    private BettingPriceRangeDao bettingPriceRangeDao;

    @Autowired
    private BettingPriceRangeMapper bettingPriceRangeMapper;


    public BettingPriceRange findOne(long id) {
        return bettingPriceRangeMapper.selectByPrimaryKey(id);
    }

   /**
    * 查询投票信息
    * @author tansitao
    * @time 2018/9/14 10:45 
    */
    public List<BettingPriceRange> findByPeriodId (long periodId){
        return bettingPriceRangeMapper.selectByPeriodId(periodId);
    }

    /***
     * 根据周期查询投票区域
     * @author yangch
     * @time 2018.09.17 21:42 
     * @param periodId
     */
    public List<BettingPriceRange> findAllByPeriodId(long periodId){
        return bettingPriceRangeDao.findAllByPeriodId(periodId);
    }

    /**
     * 计算中奖的价格区间（注意：传入的集合范围中，应该只有0个或1个价格范围中奖）
     * @author yangch
     * @time 2018.09.17 21:42 
     * @param list
     * @param determineGuessPrice
     * @return
     */
    public Optional<BettingPriceRange> determinePriceRange(List<BettingPriceRange> list,
                                                           BigDecimal determineGuessPrice){
        if(null == list
                || null == determineGuessPrice){
            return Optional.empty();
        }

        return list.stream().filter(bettingPriceRange ->
                    determineGuessPrice.compareTo(bettingPriceRange.getBeginRange()) >=0
                    && determineGuessPrice.compareTo(bettingPriceRange.getEndRange()) <=0 ).findFirst();
    }

    /**
     * 计算中奖的价格区间
     * @author yangch
     * @time 2018.09.17 21:42 
     * @param periodId
     * @param determineGuessPrice
     * @return
     */
    public Optional<BettingPriceRange> determinePriceRange(long periodId, BigDecimal determineGuessPrice){
        return determinePriceRange(findAllByPeriodId(periodId), determineGuessPrice);
    }


    public PriceRangeService getService(){
        return SpringContextUtil.getBean(PriceRangeService.class);
    }
}
