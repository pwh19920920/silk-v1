package com.spark.bitrade.service;

import com.spark.bitrade.dao.ExchangeMemberDiscountRuleDao;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.ExchangeMemberDiscountRule;
import com.spark.bitrade.util.SpringContextUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 币币交易--会员优惠规则
 * @author yangch
 * @time 2018.08.28 10:35
 */

@Service
@Slf4j
public class ExchangeMemberDiscountRuleService {
    @Autowired
    private ExchangeMemberDiscountRuleDao dao;
    @Autowired
    private ExchangeCoinService exchangeCoinService;

    //内存中缓存会员的折扣率，缓存格式= Map< 会员id, map<交易对, 买、卖折扣率> >
    private static Map<String,Map<String, DiscountRule>> mapDiscountRuleCache = null;
    private DiscountRule defaulDiscountRule = new DiscountRule(BigDecimal.ZERO, BigDecimal.ZERO);

    //刷新缓存
    public void flushCache(){
        if(null == mapDiscountRuleCache) {
            mapDiscountRuleCache = new HashMap<>();
        }

        Map<String,Map<String, DiscountRule>> _mapDiscountRuleCache = new HashMap<>();

        List<Long> memberIds = dao.findMemberIds();
        if(null !=memberIds) {
            memberIds.stream().forEach(m->_mapDiscountRuleCache.put(m.toString(), new HashMap<>()));
        }

        List<ExchangeMemberDiscountRule> listRules = dao.findAll();
        if(null != listRules){
            //List<ExchangeCoin> listCoin = exchangeCoinService.findAllEnabled();

            listRules.stream().forEach(rule -> {
                _mapDiscountRuleCache.get(rule.getMemberId().toString()).put(rule.getSymbol().toUpperCase(),
                        new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount()));
                //处理 “*”号的规则
                /*if( "*".equals(rule.getSymbol().trim()) ) {
                    Map<String, DiscountRule> mapMember = _mapDiscountRuleCache.get(rule.getMemberId().toString());

                    if(null !=listCoin) {
                        DiscountRule discountRule =  new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount());
                        listCoin.stream().forEach(exchangeCoin ->
                                mapMember.put(exchangeCoin.getSymbol().toUpperCase(), discountRule));
                    } else {
                        log.warn("交易对列表为空");
                    }
                } else {
                    _mapDiscountRuleCache.get(rule.getMemberId().toString()).put(rule.getSymbol().toUpperCase(),
                            new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount()));
                }*/
            });
        }

        mapDiscountRuleCache = _mapDiscountRuleCache;
    }

    public void flushCache(long memberId) {
        //更新缓存数据
        if(null == mapDiscountRuleCache) {
            getService().flushCache();
        } else {
            List<ExchangeMemberDiscountRule> list = dao.findAllByMemberId(memberId);
            Map<String, DiscountRule> mapMember = new HashMap<>();
            list.stream().forEach(rule -> {
                mapMember.put(rule.getSymbol().toUpperCase(),
                        new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount()));
                //处理 “*”号的规则
                /*if ("*".equals(rule.getSymbol().trim())) {
                    List<ExchangeCoin> listCoin = exchangeCoinService.findAllEnabled();
                    if (null != listCoin) {
                        DiscountRule discountRule = new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount());
                        listCoin.stream().forEach(exchangeCoin ->
                                mapMember.put(exchangeCoin.getSymbol().toUpperCase(), discountRule));
                    } else {
                        log.warn("交易对列表为空");
                    }
                } else {
                    mapMember.put(rule.getSymbol().toUpperCase(),
                            new DiscountRule(rule.getFeeBuyDiscount(), rule.getFeeSellDiscount()));
                }*/
            });

            mapDiscountRuleCache.put(String.valueOf(memberId), mapMember);
        }
    }

    public Map<String,Map<String, DiscountRule>> getMapDiscountRuleCache(){
        return mapDiscountRuleCache;
    }

    public Map<String, DiscountRule> getMapDiscountRuleCache(long memberId){
        return mapDiscountRuleCache.get(String.valueOf(memberId));
    }

    /**
     * 根据会员id和交易对获取折扣率
     *
     * @param memberId 会员id
     * @param symbol 交易对
     * @return
     */
    public DiscountRule getDiscountRule(long memberId, String symbol) {
        if(null != symbol) {
            symbol = symbol.toUpperCase();
        }

        if(null == mapDiscountRuleCache) {
            getService().flushCache();
        }

        Map<String, DiscountRule> mapMember = mapDiscountRuleCache.get(String.valueOf(memberId));
        if(null == mapMember) {
            return defaulDiscountRule;
        }

        DiscountRule discountRule = mapMember.get(symbol);
        if(null != discountRule){
            return discountRule;
        }

        //处理“*”号的规则
        return mapMember.getOrDefault("*", defaulDiscountRule);
    }

    /**
     * 判断帐号是否在规则中
     * @param memberId 帐号ID
     * @return
     */
    public boolean exist(long memberId) {
        if(null == mapDiscountRuleCache) {
            getService().flushCache();
        }

        if(null == mapDiscountRuleCache.get(String.valueOf(memberId))) {
            return false;
        }

       return true;
    }

    public ExchangeMemberDiscountRule save(ExchangeMemberDiscountRule entity) {
        /*ExchangeMemberDiscountRule newEntity = dao.saveAndFlush(entity);

        //更新缓存数据
        getService().flushCache(entity.getMemberId());

        return newEntity;*/
        return dao.saveAndFlush(entity);
    }

    public ExchangeMemberDiscountRule findOne(long id){
        return dao.findOne(id);
    }

    public void delete(long id){
        /*ExchangeMemberDiscountRule entity = getService().findOne(id);
        if(null != entity) {
            dao.delete(id);

            //更新缓存数据
            getService().flushCache(entity.getMemberId());
        }*/
        dao.delete(id);
    }

    public ExchangeMemberDiscountRuleService getService(){
        return SpringContextUtil.getBean(ExchangeMemberDiscountRuleService.class);
    }


    @Data
    public class DiscountRule{
        private BigDecimal buyDiscount; //买折扣率
        private BigDecimal sellDiscount;//卖折扣率

        public DiscountRule(BigDecimal buyDiscount, BigDecimal sellDiscount){
            if(buyDiscount.compareTo(BigDecimal.ONE)>0){
                this.buyDiscount = BigDecimal.ONE;
            } else {
                this.buyDiscount = buyDiscount;
            }

            if(sellDiscount.compareTo(BigDecimal.ONE)>0){
                this.sellDiscount = BigDecimal.ONE;
            } else {
                this.sellDiscount = sellDiscount;
            }
        }
    }
}
