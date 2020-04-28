package com.spark.bitrade.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PageModel;
import com.spark.bitrade.dao.CoinDao;
import com.spark.bitrade.dao.OtcCoinDao;
import com.spark.bitrade.dto.CoinDto;
import com.spark.bitrade.entity.Coin;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.entity.QCoin;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rongyu
 * @description
 * @date 2017/12/29 14:50
 */
@Service
public class CoinService extends BaseService {
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private OtcCoinDao otcCoinDao;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Coin> query(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<Coin> list;
        JPAQuery<Coin> jpaQuery = queryFactory.selectFrom(QCoin.coin);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());//添加总条数
    }

    @Cacheable(cacheNames = "coin", key = "'entity:coin:'+#name")
    public Coin findOne(String name) {
        return coinDao.findOne(name);
    }

    @Cacheable(cacheNames = "coin", key = "'entity:coinUnit:'+#unit")
    public Coin findByUnit(String unit) {
        return coinDao.findByUnit(unit);
    }

    /**
     * 不查缓存 直接查数据库 兼容新增字段的缓存问题
     * @param unit
     * @return
     */
    public Coin findByUnitNew(String unit){
        return coinDao.findByUnit(unit);
    }

    @CacheEvict(cacheNames = "coin", allEntries = true) //数据量不大，保存是清理所有
    public Coin save(Coin coin) {
        return coinDao.save(coin);
    }

    @Cacheable(cacheNames = "coin", key = "'entity:coin:all'")
    public List<Coin> findAll() {
        return coinDao.findAll();
    }

    public List<Coin> findAllCoinByOtc() {
        List<String> supportUnits = otcCoinDao.findAll().stream().map(x -> x.getUnit()).collect(Collectors.toList());
        if (supportUnits.size() > 0) {
            return coinDao.findAllByOtc(supportUnits);
        } else {
            return null;
        }
    }

    public Page<Coin> pageQuery(Integer pageNo, Integer pageSize) {
        //排序方式 (需要倒序 这样    Criteria.sort("id","createTime.desc") ) //参数实体类为字段名
        Sort orders = Criteria.sortStatic("sort");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<Coin> specification = new Criteria<Coin>();
        return coinDao.findAll(specification, pageRequest);
    }

    @Cacheable(cacheNames = "coin", key = "'entity:coinCanWithDraw:all'")
    public List<Coin> findAllCanWithDraw() {
        return coinDao.findAllByCanWithdrawAndStatusAndHasLegal(BooleanEnum.IS_TRUE, CommonStatus.NORMAL, false);
    }


    @ReadDataSource
    @Cacheable(cacheNames = "coin", key = "'entity:coinCanWithDraw:'+#unit")
    public Coin findCanWithDraw(String unit) {
        return coinDao.findByUnitAndCanWithdrawAndStatusAndHasLegal(unit,BooleanEnum.IS_TRUE, CommonStatus.NORMAL, false);
    }

    @CacheEvict(cacheNames = "coin", allEntries = true) //数据量不大，保存是清理所有
    public void deleteOne(String name) {
        coinDao.delete(name);
    }

    /**
     * 设置平台币
     *
     * @param coin
     */
    @Transactional(rollbackFor = Exception.class)
    public void setPlatformCoin(Coin coin) {
        List<Coin> list = coinDao.findAll();
        list.stream().filter(x ->
                !x.getName().equals(coin.getName())
        ).forEach(x -> {
            x.setIsPlatformCoin(BooleanEnum.IS_FALSE);
            coinDao.save(x);
        });
        coin.setIsPlatformCoin(BooleanEnum.IS_TRUE);
        coinDao.saveAndFlush(coin);
        OtcCoin otcCoin = otcCoinDao.findOtcCoinByUnit(coin.getUnit());
        if (otcCoin != null) {
            otcCoin.setIsPlatformCoin(BooleanEnum.IS_TRUE);
            otcCoinDao.saveAndFlush(otcCoin);
        }
        List<OtcCoin> list1 = otcCoinDao.findAll();
        list1.stream().filter(x ->
                !x.getUnit().equals(coin.getUnit())
        ).forEach(x -> {
            x.setIsPlatformCoin(BooleanEnum.IS_FALSE);
            otcCoinDao.save(x);
        });
    }

    public Coin queryPlatformCoin() {
        return coinDao.findCoinByIsPlatformCoin(BooleanEnum.IS_TRUE);
    }

    /**
     * @Description: 查询所有合法币种
     * @author rongyu
     */
    public List<Coin> findLegalAll() {
        return (List<Coin>) coinDao.findAll(QCoin.coin.hasLegal.eq(true));
    }

    public Page<Coin> findAll(Predicate predicate, Pageable pageable) {
        return coinDao.findAll(predicate, pageable);
    }
    /**
     * 获取条件查询的所有记录
     * @author shenzucai
     * @time 2018.06.13 15:51
     * @param predicate
     * @return true
     */
    public List<Coin> findAll(Predicate predicate) {
        PageModel pageModel = new PageModel();
        pageModel.setPageSize(60000);
        List<String> stringList = new ArrayList<String>();
        stringList.add("sort");
        pageModel.setProperty(stringList);
        List<Sort.Direction> directionList = new ArrayList<Sort.Direction>();
        directionList.add(Sort.Direction.ASC);
        pageModel.setDirection(directionList);
        Iterable<Coin> geted = coinDao.findAll(predicate, pageModel.getPageable());
        List<Coin> list = Lists.newArrayList();
        geted.forEach(single ->{list.add(single);});
        return list;
    }


    public Page findLegalCoinPage(PageModel pageModel) {
        BooleanExpression eq = QCoin.coin.hasLegal.eq(true);
        return coinDao.findAll(eq, pageModel.getPageable());
    }

    public List<String> getAllCoinName() {
        List<String> list = coinDao.findAllName();
        return list;
    }

    //add by yangch 时间： 2018.04.29 原因：合并
    public List<CoinDto> getAllCoinNameAndUnit() {
        List<CoinDto> allNameAndUnit = coinDao.findAllNameAndUnit();
        return allNameAndUnit;
    }

    public List<String> getAllCoinNameLegal() {
        return coinDao.findAllCoinNameLegal();
    }

    /**
     * 查询所有的非代币币种
     * @author shenzucai
     * @time 2018.04.22 11:34
     * @param
     * @return true
     */
    public List<Coin> findAllBaseCoin(){
        //add|edit|del by shenzucai 时间： 2018.05.25 原因：
        return coinDao.findAllByBaseCoinUnit();
    }

    /**
     * 后台货币Coin总额导出
     * @author Zhang Yanjun
     * @time 2018.09.06 17:16
     * @param
     */
    @ReadDataSource
    public List<Coin> findAllOrderBySort(){
        return coinDao.findCoinOrderBySort();
    }

    //查询币种介绍
    @ReadDataSource
    public String findContentByUnit(String unit){
        return coinDao.findContentByUnit(unit);
    }
}
