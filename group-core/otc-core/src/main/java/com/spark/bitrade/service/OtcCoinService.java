package com.spark.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.dao.OtcCoinDao;
import com.spark.bitrade.entity.OtcCoin;
import com.spark.bitrade.entity.QOtcCoin;
import com.spark.bitrade.entity.SilkDataDist;
import com.spark.bitrade.pagination.Criteria;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.pagination.Restrictions;
import com.spark.bitrade.service.Base.BaseService;
import com.sparkframework.sql.model.Model;
import org.apache.commons.beanutils.ConvertUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.BooleanEnum.IS_TRUE;

/**
 * @author rongyu
 * @description
 * @date 2018/1/11 13:45
 */
@Service
public class OtcCoinService extends BaseService {
    @Autowired
    private OtcCoinDao otcCoinDao;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private ISilkDataDistService iSilkDataDistService;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<OtcCoin> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<OtcCoin> list;
        JPAQuery<OtcCoin> jpaQuery = queryFactory.selectFrom(QOtcCoin.otcCoin);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }


    @CacheEvict(cacheNames = "otcCoin", allEntries = true) //数据量不大，保存是清理所有
    public OtcCoin save(OtcCoin otcCoin) {
        return otcCoinDao.save(otcCoin);
    }

    @Cacheable(cacheNames = "otcCoin", key = "'entity:otcCoin:all'")
    public List<OtcCoin> findAll() {
        return otcCoinDao.findAll();
    }

    @Cacheable(cacheNames = "otcCoin", key = "'entity:otcCoin:'+#id")
    public OtcCoin findOne(Long id) {
        return otcCoinDao.findOne(id);
    }

    /**
     * 根据币种id，获取币种详情
     * @param id
     * @return
     */
    public OtcCoin getOtcCoin(Long id) {
        return otcCoinDao.findOne(id);
    }

    @Cacheable(cacheNames = "otcCoin", key = "'entity:otcCoin:'+#unit")
    public OtcCoin findByUnit(String unit) {
        return otcCoinDao.findOtcCoinByUnit(unit);
    }

    //add|edit|del by tansitao 时间： 2018/11/19 原因：恢复取消的缓存
    @Cacheable(cacheNames = "otcCoin", key = "'entity:otcCoin:allNormal'")
    public List<Map<String, String>> getAllNormalCoin() throws Exception {
        //add by tansitao 时间： 2018/10/26 原因：增加币种精度
        return new Model("otc_coin")
                .field("id,name,name_cn as nameCn,unit,sell_min_amount,sort,buy_min_amount,coin_scale," +
                        "trade_max_limit as tradeMaxLimit,trade_min_limit as tradeMinLimit")
                .where("status=?", CommonStatus.NORMAL.ordinal())
                .order("sort desc").select();
    }

    //add by tansitao 时间： 2018/4/26 原因：增加获取所有币种和余额
    public List<Map<String, String>> getAllNormalCoinAndBalance(long memberId) throws Exception {
        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();


        // String sql = "SELECT a.id, a.name, a.name_cn as nameCn, a.unit, a.sell_min_amount, a.sort, a.buy_min_amount, mw.balance " +
        //         "FROM otc_coin a LEFT JOIN member_wallet mw on a.name = mw.coin_id WHERE mw.member_id = :memberId";


        //add|edit|del by  shenzucai 时间： 2018.07.05  原因：只获取有效状态的币种 start
        //add|edit|del by tansitao 时间： 2018/10/25 原因：增加需求1.3的字段
        String sql = "SELECT a.id,a.jy_rate jyRate,a.buy_jy_rate buyJyRate,a.trade_min_limit as tradeMinLimit,a.trade_max_limit as tradeMaxLimit, " +
                "a.coin_scale coinScale, a.general_fee_coin_unit generalFeeCoinUnit, a.general_fee generalFee, a.general_discount_coin_unit generalDiscountCoinUnit, a.general_discount_rate generalDiscountRate, a.general_discount_coin_scale generalDiscountCoinScale, a.general_buy_min_balance generalBuyMinBalance," +
                "a.name, a.name_cn as nameCn, a.unit, a.sell_min_amount, a.sort, a.buy_min_amount, mw.balance " +
                "FROM otc_coin a LEFT JOIN member_wallet mw on a.name = mw.coin_id WHERE mw.member_id = :memberId and a.status = 0";
        //add|edit|del by  shenzucai 时间： 2018.07.05  原因：只获取有效状态的币种 end
        Query query = em.createNativeQuery(sql);
        //设置结果转成Map类型,
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List rowslist = query.setParameter("memberId", memberId).getResultList();
        rowslist.stream().forEachOrdered(x -> {
            Map<String, String> map = (HashMap<String, String>) x;
            map.put("id", String.valueOf(map.get("id")));
            map.put("sort", String.valueOf(map.get("sort")));
            mapList.add(map);
        });
        return mapList;
    }

    @Cacheable(cacheNames = "otcCoin", key = "'entity:otcCoin:allNormal2'")
    public List<OtcCoin> getNormalCoin() {
        return otcCoinDao.findAllByStatus(CommonStatus.NORMAL);
    }

    /**
     * @author rongyu
     * @description 分页请求
     * @date 2018/1/11 15:04
     */
    public Page<OtcCoin> pageQuery(Integer pageNo, Integer pageSize, String name, String nameCn) {
        //排序方式
        Sort orders = Criteria.sortStatic("sort");
        //分页参数
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, orders);
        //查询条件
        Criteria<OtcCoin> specification = new Criteria<OtcCoin>();
        specification.add(Restrictions.like("name", name, false));
        specification.add(Restrictions.like("nameCn", nameCn, false));
        return otcCoinDao.findAll(specification, pageRequest);
    }

    @CacheEvict(cacheNames = "otcCoin", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deletes(Long[] ids) {
        for (long id : ids) {
            otcCoinDao.delete(id);
        }
    }

    public Page<OtcCoin> findAll(Predicate predicate, Pageable pageable) {
        return otcCoinDao.findAll(predicate, pageable);
    }

    /**
     * 获取服务费率
     */
    public BigDecimal getServiceRate(String coin) {
        SilkDataDist silkData = iSilkDataDistService.findByIdAndKey("TRADE_SELL_SERVICE_RATE", "RATE_" + coin.toUpperCase());
        if (silkData != null && silkData.getStatus() == IS_TRUE) {
            BigDecimal rate = ConvertUtils.lookup(BigDecimal.class).convert(BigDecimal.class, silkData.getDictVal());
            return BigDecimal.ZERO.compareTo(rate) > 0 ? BigDecimal.ZERO : rate;
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取法币手续费归集账户
     *
     * @return
     */
    public Long getOtcSummarizeAccount() {
        SilkDataDist silkData = iSilkDataDistService.findByIdAndKey("FEE_COLLECTION", "LEGAL_TENDER_FEE_COLLECTION");
        if (silkData != null && silkData.getStatus() == IS_TRUE) {
            return Long.valueOf(silkData.getDictVal());
        } else {
            return new Long(0);
        }
    }
    /**
     * 获取法币手续费归集账户
     *
     * @return
     */
    public Long getOtcSummarizeAccountOfUSDC() {
        SilkDataDist silkData = iSilkDataDistService.findByIdAndKey("MEMBER_SYSTEM_CONFIG", "TOTAL_ACCOUNT_ID");
        if (silkData != null && silkData.getStatus() == IS_TRUE) {
            return Long.valueOf(silkData.getDictVal());
        } else {
            return new Long(0);
        }
    }

}
