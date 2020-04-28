package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.spark.bitrade.constant.ExchangeCoinDisplayArea;
import com.spark.bitrade.dao.ExchangeCoinRepository;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeOrder;
import com.spark.bitrade.mapper.dao.ExchangeCoinMapper;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.service.Base.TopBaseService;
import com.spark.bitrade.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ExchangeCoinService {
    @Autowired
    private ExchangeCoinRepository coinRepository;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Autowired
    private ExchangeCoinMapper exchangeCoinMapper;

    @Cacheable(cacheNames = "exchangeCoinAll", key = "'entity:exchangeCoinEnabled:All'")
    public List<ExchangeCoin> findAllEnabled() {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            criteriaQuery.where(criteriaBuilder.equal(enable, 1));
            return null;
        };
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort sort = new Sort(order);
        return coinRepository.findAll(spec, sort);
    }

    /***
     * 根据显示区域查询交易区币种
     * @author yangch
     * @time 2018.06.28 14:16 
       * @param displayArea
     */
    @Cacheable(cacheNames = "exchangeCoinAll", key = "'entity:exchangeCoinEnabled:'+#displayArea.name()")
    public List<ExchangeCoin> findAllByDisplayArea(ExchangeCoinDisplayArea displayArea){
        return coinRepository.queryAllByDisplayArea(displayArea);
    }

    @Cacheable(cacheNames = "exchangeCoin", key = "'entity:exchangeCoinFlag:'+#flag")
    public List<ExchangeCoin> findAllByFlag(int flag) {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            Path<Integer> flagPath = root.get("flag");
            criteriaQuery.where(criteriaBuilder.equal(enable, 1));
            criteriaQuery.where(criteriaBuilder.equal(flagPath, flag));
            return null;
        };
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort sort = new Sort(order);
        return coinRepository.findAll(spec, sort);
    }

    @Cacheable(cacheNames = "exchangeCoin", key = "'entity:exchangeCoin:'+#id")
    public ExchangeCoin findOne(String id) {
        return coinRepository.findOne(id);
    }

    @CacheEvict(cacheNames = {"exchangeCoinSymbol","exchangeCoinAll","exchangeCoin"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deletes(String[] ids) {
        for (String id : ids) {
            coinRepository.delete(id);
        }
    }

    @CacheEvict(cacheNames = {"exchangeCoinAll"}, allEntries = true)
    public void flushAll(){
    }

    //@CacheEvict(cacheNames = {"exchangeCoinSymbol","exchangeCoinAll","exchangeCoin"}, allEntries = true)
    @CacheEvict(cacheNames = {"exchangeCoinSymbol", "exchangeCoin"}, allEntries = true)
    public ExchangeCoin save(ExchangeCoin exchangeCoin) {
        redisTemplate.delete("exchangeCoin:"+exchangeCoin.getSymbol());
        return coinRepository.save(exchangeCoin);
    }

    public Page<ExchangeCoin> pageQuery(int pageNo, Integer pageSize) {
        Sort orders = Criteria.sortStatic("sort");
        PageRequest pageRequest = new PageRequest(pageNo - 1, pageSize, orders);
        return coinRepository.findAll(pageRequest);
    }

    @Cacheable(cacheNames = "exchangeCoinSymbol", key = "'entity:exchangeCoin:'+#symbol")
    public ExchangeCoin findBySymbol(String symbol) {
        return coinRepository.findBySymbol(symbol);
        /*ExchangeCoin coin = (ExchangeCoin)redisTemplate.opsForValue().get("exchangeCoin:"+symbol);
        if(coin == null ) {
            coin = coinRepository.findBySymbol(symbol);
            redisTemplate.opsForValue().set("exchangeCoin:"+symbol, coin);
            //redisTemplate.expire("exchangeCoin:"+symbol,30, TimeUnit.MINUTES);
        }

        return coin;*/
    }

   // @Cacheable(cacheNames = "exchangeCoinAll", key = "'entity:exchangeCoin:All'")
    public List<ExchangeCoin> findAll() {
        return coinRepository.findAll();
    }

    @Cacheable(cacheNames = "exchangeCoinSymbol", key = "'entity:exchangeCoin:is-'+#symbol")
    public boolean isSupported(String symbol) {
        ExchangeCoin exchangeCoin = getService().findBySymbol(symbol);
        if(exchangeCoin!=null && exchangeCoin.getEnable()==1){
            return true;
        }

        return false;
        //return findBySymbol(symbol) != null;
    }

    public Page<ExchangeCoin> findAll(Predicate predicate, Pageable pageable) {
        return coinRepository.findAll(predicate, pageable);
    }

    @Cacheable(cacheNames = "exchangeCoinAll", key = "'entity:exchangeCoinBaseSymbol:All'")
    public List<String> getBaseSymbol() {
        return coinRepository.findBaseSymbol();
    }

    //add by yangch 时间： 2018.05.24 原因：代码合并
    public List<String> getCoinSymbol(String baseSymbol) {
        return coinRepository.findCoinSymbol(baseSymbol);
    }

    //add by yangch 时间： 2018.05.31 原因：代码合并
    public List<String> getAllCoin(){
        return coinRepository.findAllCoinSymbol();
    }

    public ExchangeCoinService getService(){
        return SpringContextUtil.getBean(ExchangeCoinService.class);
    }

    public List<Map<String,Object>> getExchangeCoin(String symbol, String coinSymbol, String baseSymbol){
        return exchangeCoinMapper.findTraderDiscount(symbol,coinSymbol,baseSymbol);
    }

    public List<ExchangeCoin> selectByShow(){
        return coinRepository.findByIsShow(0);
    }

}
