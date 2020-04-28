package com.spark.bitrade.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.constant.Currency;
import com.spark.bitrade.dao.AdvertiseDao;
import com.spark.bitrade.dao.OtcCoinDao;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.*;
import com.spark.bitrade.exception.InconsistencyException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.mapper.dao.AdvertiseMapper;
import com.spark.bitrade.pagination.PageResult;
import com.spark.bitrade.service.Base.BaseService;
import com.spark.bitrade.util.*;
import com.sparkframework.sql.DB;
import com.sparkframework.sql.DataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.BooleanEnum.IS_FALSE;
import static com.spark.bitrade.constant.BooleanEnum.IS_TRUE;
import static com.spark.bitrade.entity.QAdvertise.advertise;
import static com.spark.bitrade.util.BigDecimalUtils.mulRound;
import static com.spark.bitrade.util.BigDecimalUtils.rate;

/**
 * @author Zhang Jinwei
 * @date 2017年12月07日
 */
@Service
@Slf4j
public class AdvertiseService extends BaseService {

    @Autowired
    private AdvertiseDao advertiseDao;
    @Autowired
    private OtcCoinDao otcCoinDao;
    @Autowired
    private MemberWalletService memberWalletService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AdvertiseMapper advertiseMapper;

    @Autowired
    private CoinService coinService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Advertise> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<Advertise> list;
        JPAQuery<Advertise> jpaQuery = queryFactory.selectFrom(advertise);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    public Advertise findOne(Long id) {
        return advertiseDao.findOne(id);
    }

    /**
      * 通过只读数据库查询广告
      * @author tansitao
      * @time 2018/7/17 14:42 
      */
    @ReadDataSource
    public Advertise findById(Long id) {
        return advertiseMapper.findById(id);
    }


    public MemberAdvertiseDetail findOne(Long id, Long memberId) {
        Advertise advertise = advertiseDao.findAdvertiseByIdAndMemberIdAndStatusNot(id, memberId, AdvertiseControlStatus.TURNOFF);
        if (advertise != null) {
            return MemberAdvertiseDetail.toMemberAdvertiseDetail(advertise);
        } else {
            return null;
        }
    }

    public Advertise find(long id, long memberId) {
        return advertiseDao.findByIdAndMemberId(id, memberId);
    }

    public Advertise saveAdvertise(Advertise advertise) {
        return advertiseDao.save(advertise);
    }

    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true)
    public Advertise save(Advertise advertise){
        return advertiseDao.save(advertise);
    }

    @Transactional(rollbackFor = Exception.class)
    public int turnOffBatch(AdvertiseControlStatus status, Long[] ids) {
        return advertiseDao.alterStatusBatch(status, new Date(), ids);
    }

    //edit by yangch 时间： 2018.04.29 原因：合并
    /*public Advertise modifyAdvertise(Advertise advertise, Advertise advertise1) {
        return advertise1.toAdvertise(advertise);
    }*/
    @Transactional(rollbackFor = Exception.class)
    public Advertise modifyAdvertise(Advertise advertise, Advertise old) {
        if (advertise.getPriceType() == PriceType.MUTATIVE) {
            //变化的
            old.setPriceType(PriceType.MUTATIVE);
            old.setPremiseRate(advertise.getPremiseRate());
        } else {
            //固定的
            old.setPriceType(PriceType.REGULAR);
            old.setPrice(advertise.getPrice());
        }
        if (advertise.getAuto().isIs()) {
            old.setAuto(BooleanEnum.IS_TRUE);
            old.setAutoword(advertise.getAutoword());
        } else {
            old.setAuto(BooleanEnum.IS_FALSE);
        }
        old.setMinLimit(advertise.getMinLimit());
        old.setMaxLimit(advertise.getMaxLimit());
        old.setTimeLimit(advertise.getTimeLimit());
        old.setRemark(advertise.getRemark());
        old.setPayMode(advertise.getPayMode());
        old.setNumber(advertise.getNumber());
        old.setRemainAmount(advertise.getNumber());
        //变更为下架状态
        old.setStatus(AdvertiseControlStatus.PUT_OFF_SHELVES);

        //add|edit|del by tansitao 时间： 2018/11/1 原因：交易平台1.3需求
        old.setNeedBindPhone(advertise.getNeedBindPhone());
        old.setNeedPutonDiscount(advertise.getNeedPutonDiscount());
        old.setNeedRealname(advertise.getNeedRealname());
        old.setNeedTradeTimes(advertise.getNeedTradeTimes());
        old.setMaxTradingOrders(advertise.getMaxTradingOrders()); //add by tansitao 时间： 2018/11/21 原因：增加同时最大交易数
        return advertiseDao.save(old);

    }

    public List<MemberAdvertise> getAllAdvertiseByMemberId(Long memberId, Sort sort) {
        List<Advertise> list = advertiseDao.findAllByMemberIdAndStatusNot(memberId, AdvertiseControlStatus.TURNOFF, sort);
        return list.stream().map(x ->
                MemberAdvertise.toMemberAdvertise(x)
        ).collect(Collectors.toList());
    }

    //add by tansitao 时间： 2018/6/9 原因：获取所有用户的上架广告
    public List<Advertise> getAllOnAdvertiseByMemberId(Long memberId) {
        List<Advertise> list = advertiseDao.findAllByMemberIdAndStatus(memberId, AdvertiseControlStatus.PUT_ON_SHELVES);
        return list;
    }

    public List<ScanAdvertise> getAllExcellentAdvertise(AdvertiseType type, List<Map<String, String>> list) throws SQLException, DataException {
        List<ScanAdvertise> excellents = new ArrayList<>();
        String sql = "SELECT\n" +
                "\td.*\n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tc.coin_id,\n" +
                "\t\t\t(\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\tWHEN c.price_type = 0\n" +
                "\t\t\t\tAND c.price = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tWHEN c.price_type = 1\n" +
                "\t\t\t\tAND round(((c.premise_rate + 100) / 100 * ?),2) = b.minPrice THEN\n" +
                "\t\t\t\t\tc.id\n" +
                "\t\t\t\tEND\n" +
                "\t\t\t) advertise_id,\n" +
                "\t\t\tb.minPrice\n" +
                "\t\tFROM\n" +
                "\t\t\tadvertise c\n" +
                "\t\tJOIN (\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\ta.coin_id,\n" +
                (type.equals(AdvertiseType.SELL) ? "\t\t\t\tmin(\n" : "\t\t\t\tmax(\n") +
                "\t\t\t\t\tCASE a.price_type\n" +
                "\t\t\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\t\t\ta.price\n" +
                "\t\t\t\t\tELSE\n" +
                "\t\t\t\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\t\t\t\tEND\n" +
                "\t\t\t\t) minPrice,\n" +
                "\t\t\t\ta.advertise_type,\n" +
                "\t\t\t\ta.`status`\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tadvertise a\n" +
                "\t\t\tWHERE\n" +
                "\t\t\t\ta. STATUS = 0\n" +
                "\t\t\tAND a.advertise_type = ?\n" +
                "\t\t\tGROUP BY\n" +
                "\t\t\t\ta.coin_id\n" +
                "\t\t) b ON c.coin_id = b.coin_id\n" +
                "\t\tAND c.advertise_type = b.advertise_type\n" +
                "\t\tAND c.`status` = b. STATUS\n" +
                "\t\tAND c.coin_id = ?\n" +
                "\t) d\n" +
                "WHERE\n" +
                "\td.advertise_id IS NOT NULL\n" +
                "GROUP BY\n" +
                "\td.coin_id";
        list.parallelStream()
                .forEachOrdered((Map<String, String> x) -> {
                    OtcCoin otcCoin = otcCoinDao.findOtcCoinByUnitAndStatus(x.get("name"), CommonStatus.NORMAL);
                    if (otcCoin != null) {
                        try {
                            List<Map<String, String>> mapList = DB.query(sql, x.get("price"), x.get("price"), type.ordinal(), otcCoin.getId());
                            if (mapList.size() > 0) {
                                Advertise advertise = advertiseDao.findOne(Long.valueOf(mapList.get(0).get("advertise_id")));
                                Member member = advertise.getMember();
                                excellents.add(ScanAdvertise
                                        .builder()
                                        .advertiseId(advertise.getId())
                                        .coinId(otcCoin.getId())
                                        .coinName(otcCoin.getName())
                                        .coinNameCn(otcCoin.getNameCn())
                                        .createTime(advertise.getCreateTime())
                                        .maxLimit(advertise.getMaxLimit())
                                        .minLimit(advertise.getMinLimit())
                                        .memberName(member.getUsername())
                                        .avatar(member.getAvatar())
                                        .level(member.getMemberLevel().ordinal())
                                        .payMode(advertise.getPayMode())
                                        .unit(otcCoin.getUnit())
                                        .remainAmount(advertise.getRemainAmount())
                                        .transactions(member.getTransactions())
                                        .price(BigDecimalUtils.round(Double.valueOf(mapList.get(0).get("minPrice")), 2))
                                        .build()
                                );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        return excellents;
    }

    /**
      * 只读数据库分页查询
      * @author tansitao
      * @time 2018/7/17 16:53 
      */
    @ReadDataSource
    //add by tansitao 时间： 2018/11/20 原因：优化OTC广告列表
    @Cacheable(cacheNames = "otcAdvertise", key = "'entity:otcAdvertise:' + #coinId + '_' + #advertiseType + '_' + #pageNo")
    public PageInfo<OtcAdvertise> pageAdvertise(int pageNo, int pageSize,  long coinId, long advertiseType, int status, double marketPrice, int coinScale)
    {
        com.github.pagehelper.Page<OtcAdvertise> page = PageHelper.startPage(pageNo, pageSize);
        advertiseMapper.pageQueryByOtcCoin( coinId,  advertiseType,  status, marketPrice, coinScale);
        return page.toPageInfo();
    }

    /**
      * 通过广告类型和数量获取某些用户的广告
      * @author tansitao
      * @time 2019/1/7 15:10 
      */
    /**
     * 通过广告类型和数量获取某些用户的广告,以及价格（浮动的时候为市场价格（当广告本身没有价格的时候备用），固定的时候为广告价格）
     * @author shenzucai
     * @time 2019.04.29 15:46
     * @param memberIds
     * @param number
     * @param marketPrice
     * @param advertiseType
     * @param coinId
     * @param currencyId
     * @return true
     */
    public List<Advertise> getByMemberIds(List<Long> memberIds, BigDecimal number,BigDecimal marketPrice, AdvertiseType advertiseType, Long coinId,Long currencyId){
        return advertiseMapper.findByMemberIds(memberIds, number,marketPrice, advertiseType.getOrdinal(), coinId,currencyId);
    }
/**
  * 通过广告类型和数量获取某些用户的广告
  * @author tansitao
  * @time 2019/1/7 15:10 
  */
    /**
     * 通过广告类型和数量获取某些用户的广告,以及价格（浮动的时候为市场价格（当广告本身没有价格的时候备用），固定的时候为广告价格）
     * @author shenzucai
     * @time 2019.04.29 15:46
     * @param memberIds
     * @param advertiseType
     * @param coinId
     * @param currencyId
     * @return true
     */
    public List<Advertise> getByMoneyMemberIds(List<Long> memberIds, BigDecimal money, AdvertiseType advertiseType, Long coinId,Long currencyId){
        return advertiseMapper.findByMemberIdAndMoney(memberIds, money, advertiseType.getOrdinal(), coinId,currencyId);
    }

    /**
      * 只读数据库分页查询
      * @author tansitao
      * @time 2018/7/17 16:53 
      */
    @ReadDataSource
    //add by tansitao 时间： 2018/11/20 原因：优化OTC广告列表
    @Cacheable(cacheNames = "otcAdvertise", key = "'entity:otcAdvertise:' + #coinId + '_' + #advertiseType + '_' + #advertiseRankType.getOrdinal() + '_' + #isPositive.getOrdinal() + '_' + #pageNo")
    public PageInfo<OtcAdvertise> pageAdvertiseRank(int pageNo, int pageSize,  long coinId, long advertiseType, int status, double marketPrice,AdvertiseRankType advertiseRankType, BooleanEnum isPositive, int coinScale)
    {
        com.github.pagehelper.Page<OtcAdvertise> page = PageHelper.startPage(pageNo, pageSize);
        advertiseMapper.pageQueryByOtcCoinByRank( coinId,  advertiseType,  status, marketPrice, advertiseRankType.getOrdinal(), isPositive.getOrdinal(), coinScale);
        return page.toPageInfo();
    }

    public SpecialPage<ScanAdvertise> paginationAdvertise(int pageNo, int pageSize, OtcCoin otcCoin, AdvertiseType advertiseType, double marketPrice, int isCertified) throws SQLException, DataException {
        //add|edit|del by tansitao 时间： 2018/5/10 原因：随机排序条件
        int randomNum = GeneratorUtil.getRandomNumber(1, 3);
        //add|edit|del by tansitao 时间： 2018/5/10 原因：随机正序和倒序取值
        int randomDescNum = GeneratorUtil.getRandomNumber(1, 9);
        String randomDesc = "";
        if(randomDescNum%2 > 0)
        {
            randomDesc = "desc ";
        }
        String randomSort = "" ;

        if(randomNum == 1)
        {
            randomSort = ",a.id " + randomDesc;//add by tansitao 时间： 2018/5/10 原因：按照id排序
        }
        else if(randomNum == 2)
        {
            randomSort = ",b.transactions " + randomDesc;//add by tansitao 时间： 2018/5/10 原因：按照交易次数排序
        }
        else if(randomNum == 3)
        {
            randomSort = ",a.number " + randomDesc;//add by tansitao 时间： 2018/5/10 原因：按照广告卖币数量排序
        }
        else
        {
            randomSort = ",a.member_id " + randomDesc;//add by tansitao 时间： 2018/5/10 原因：按照用户id排序
        }
        System.out.println("================================================" + randomSort);

        SpecialPage<ScanAdvertise> specialPage = new SpecialPage<>();
        String sql = "SELECT\n" +
                "\ta.*, (\n" +
                "\t\tCASE a.price_type\n" +
                "\t\tWHEN 0 THEN\n" +
                "\t\t\ta.price\n" +
                "\t\tELSE\n" +
                "\t\t\tround(((a.premise_rate + 100) / 100 * ?),2)\n" +
                "\t\tEND\n" +
                "\t) finalPrice,\n" +
                "\tb.avatar,\n" +
                "\tb.username,\n" +
                "\tb.member_level,\n" +
                "\tb.transactions\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "JOIN member b ON a.member_id = b.id\n" +
                (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                "AND a.coin_id = ?\n" +
                "AND a.advertise_type = ?\n" +
                "AND a.`status` = 0\n" +
                "ORDER BY\n" +
                //edit by fumy date:2018-06-22 reason:内部商家广告排序固定在最前
                "a.member_id not in(74655,74657,74654),\n"+
                //edit by tansitao 时间： 2018/5/10 原因：取消按照广告id排序，
                (advertiseType.equals(AdvertiseType.SELL) ? "\tfinalPrice\n" : "\tfinalPrice desc\n") +
                randomSort +
                "LIMIT ?,\n" +
                " ?";
        List<Map<String, String>> list = DB.query(sql, marketPrice, otcCoin.getId(), advertiseType.ordinal(), (pageNo - 1) * pageSize, pageSize);
        if (list.size() > 0) {
            String sql1 = "SELECT\n" +
                    "\tCOUNT(a.id) total\n" +
                    "FROM\n" +
                    "\tadvertise a\n" +
                    "JOIN member b ON a.member_id = b.id\n" +
                    (isCertified == 1 ? "AND b.member_level = 2\n" : " ") +
                    "AND a.coin_id = ?\n" +
                    "AND a.advertise_type = ?\n" +
                    "AND a.`status` = 0";
            List<Map<String, String>> list1 = DB.query(sql1, otcCoin.getId(), advertiseType.ordinal());
            Map<String, String> map = list1.get(0);
            int total = Integer.valueOf(map.get("total"));
            specialPage.setTotalElement(total);
            specialPage.setTotalPage(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
            specialPage.setContext(
                    list.stream().map((Map<String, String> x) ->
                            ScanAdvertise.builder()
                                    .price(BigDecimalUtils.round(Double.valueOf(x.get("finalPrice")), 2))
                                    .transactions(Integer.parseInt(x.get("transactions")))
                                    .remainAmount(BigDecimal.valueOf(Double.valueOf(x.get("remain_amount"))))
                                    .unit(otcCoin.getUnit())
                                    .payMode(x.get("pay_mode"))
                                    .memberName(x.get("username"))
                                    .avatar(x.get("avatar"))
                                    .minLimit(BigDecimal.valueOf(Double.valueOf(x.get("min_limit"))))
                                    .maxLimit(BigDecimal.valueOf(Double.valueOf(x.get("max_limit"))))
                                    .coinNameCn(otcCoin.getNameCn())
                                    .level(Integer.parseInt(x.get("member_level")))
                                    .coinId(otcCoin.getId())
                                    .coinName(otcCoin.getName())
                                    .advertiseId(Long.valueOf(x.get("id")))
                                    .createTime(DateUtil.strToDate(x.get("create_time")))
                                    .advertiseType(advertiseType)
                                    .build()
                    ).collect(Collectors.toList()));
        } else {
            specialPage.setTotalPage(1);
            specialPage.setTotalElement(0);
        }
        specialPage.setCurrentPage(pageNo);
        specialPage.setPageNumber(pageSize);
        return specialPage;
    }

    public Page<ScanAdvertise> paginationQuery(int pageNo, int pageSize, String country, String payMode, AdvertiseType advertiseType, Currency currency) {
        Sort.Order order1 = new Sort.Order(Sort.Direction.ASC, "price");
        Sort.Order order2 = new Sort.Order(Sort.Direction.DESC, "id");
        Sort sort = new Sort(order1, order2);
        PageRequest pageRequest = new PageRequest(pageNo, pageSize, sort);
        Specification<Advertise> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> country1 = root.get("country");
            Path<String> payMode1 = root.get("payMode");
            Path<AdvertiseType> advertiseType1 = root.get("advertiseType");
            Path<Long> currency1 = root.get("coin").get("id");
            Path<CommonStatus> status1 = root.get("status");
            Predicate predicate1 = criteriaBuilder.like(payMode1, "%" + payMode + "%");
            Predicate predicate2 = criteriaBuilder.equal(country1, country);
            Predicate predicate3 = criteriaBuilder.equal(advertiseType1, advertiseType);
            Predicate predicate4 = criteriaBuilder.equal(currency1, currency);
            Predicate predicate5 = criteriaBuilder.equal(status1, CommonStatus.NORMAL);
            if (country == null && payMode == null) {
                return criteriaBuilder.and(predicate3, predicate4, predicate5);
            } else if (country != null && payMode == null) {
                return criteriaBuilder.and(predicate2, predicate3, predicate4, predicate5);
            } else if (country == null && payMode != null) {
                return criteriaBuilder.and(predicate1, predicate3, predicate4, predicate5);
            } else {
                return criteriaBuilder.and(predicate1, predicate2, predicate3, predicate4, predicate5);
            }
        };
        Page<Advertise> page = advertiseDao.findAll(specification, pageRequest);
        //todo:得到市场价
        BigDecimal markerprice = BigDecimal.TEN;
        Page<ScanAdvertise> page1 = page.map((Advertise advertise) -> {
            Member member = advertise.getMember();
            return ScanAdvertise.builder()
                    .advertiseId(advertise.getId())
                    .coinId(advertise.getCoin().getId())
                    .coinName(advertise.getCoin().getName())
                    .coinNameCn(advertise.getCoin().getNameCn())
                    .createTime(advertise.getCreateTime())
                    .maxLimit(advertise.getMaxLimit())
                    .minLimit(advertise.getMinLimit())
                    .memberName(member.getUsername())
                    .payMode(advertise.getPayMode())
                    .unit(advertise.getCoin().getUnit())
                    .remainAmount(advertise.getRemainAmount())
                    .transactions(member.getTransactions())
                    .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                            advertise.getPrice() :
                            markerprice.multiply(advertise.getPremiseRate().divide(new BigDecimal(100)).add(BigDecimal.ONE)))
                    .build();

        });
        return page1;
    }

    public MemberAdvertiseInfo getMemberAdvertise(Member member, HashMap<String, BigDecimal> map) {
        List<Advertise> buy = advertiseDao.findAllByMemberIdAndStatusAndAdvertiseType(member.getId(), AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseType.BUY);
        List<Advertise> sell = advertiseDao.findAllByMemberIdAndStatusAndAdvertiseType(member.getId(), AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseType.SELL);
        return MemberAdvertiseInfo.builder()
                .createTime(member.getRegistrationTime())
                .emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE : IS_TRUE)
                .transactions(member.getTransactions())
                .username(member.getUsername())
                .avatar(member.getAvatar())
                .memberLevel(member.getMemberLevel()) //add by tansitao 时间： 2018/11/2 原因：增加广告主等级
                .buy(buy.stream().map(advertise -> {
                    BigDecimal markerPrice = map.get(advertise.getCoin().getUnit());
                    Member member1 = advertise.getMember();
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .coinId(advertise.getCoin().getId())
                            .coinName(advertise.getCoin().getName())
                            .coinNameCn(advertise.getCoin().getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(advertise.getCoin().getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                                    advertise.getPrice() :
                                    mulRound(markerPrice, rate(advertise.getPremiseRate()), advertise.getCoin().getCoinScale()))//edit by tansitao 时间： 2018/11/11 原因：修改为动态的获取币种精度
//                            .country(advertise.getCountry()) //add by tansitao 时间： 2018/8/15 原因：增加国家
                            .advertiseType(advertise.getAdvertiseType())//add by tansitao 时间： 2018/9/8 原因：增加广告类型
                            .needBindPhone(advertise.getNeedBindPhone())//add by tansitao 时间： 2018/11/2 原因：增加是否需要手机绑定
                            .needRealname(advertise.getNeedRealname())//add by tansitao 时间： 2018/11/2 原因：增加是否需要实名认证
                            .needTradeTimes(advertise.getNeedTradeTimes())//add by tansitao 时间： 2018/11/2 原因：增加交易次数限制
                            .level(member1.getMemberLevel().getOrdinal())
                            .build();
                }).collect(Collectors.toList()))
                .sell(sell.stream().map(advertise -> {
                    BigDecimal markerPrice = map.get(advertise.getCoin().getUnit());
                    Member member1 = advertise.getMember();
                    return ScanAdvertise.builder()
                            .advertiseId(advertise.getId())
                            .coinId(advertise.getCoin().getId())
                            .coinName(advertise.getCoin().getName())
                            .coinNameCn(advertise.getCoin().getNameCn())
                            .createTime(advertise.getCreateTime())
                            .maxLimit(advertise.getMaxLimit())
                            .minLimit(advertise.getMinLimit())
                            .memberName(member1.getUsername())
                            .payMode(advertise.getPayMode())
                            .unit(advertise.getCoin().getUnit())
                            .remainAmount(advertise.getRemainAmount())
                            .transactions(member1.getTransactions())
                            .price(advertise.getPriceType().equals(PriceType.REGULAR) ?
                                    advertise.getPrice() : mulRound(markerPrice, rate(advertise.getPremiseRate()), advertise.getCoin().getCoinScale()) //edit by tansitao 时间： 2018/11/11 原因：修改为动态的获取币种精度
                            )
//                            .country(advertise.getCountry()) //add by tansitao 时间： 2018/8/15 原因：增加国家
                            .advertiseType(advertise.getAdvertiseType())//add by tansitao 时间： 2018/9/8 原因：增加广告类型
                            .needBindPhone(advertise.getNeedBindPhone())//add by tansitao 时间： 2018/11/2 原因：增加是否需要手机绑定
                            .needRealname(advertise.getNeedRealname())//add by tansitao 时间： 2018/11/2 原因：增加是否需要实名认证
                            .needTradeTimes(advertise.getNeedTradeTimes())//add by tansitao 时间： 2018/11/2 原因：增加交易次数限制
                            .level(member1.getMemberLevel().getOrdinal())
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    /***
      * 修改广告的可用数量
      * @author yangch
      * @time 2018.07.12 17:23 
     * @param advertiseId 广告ID
     * @param amount 买入数量
     */
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true) //add by tansitao 时间： 2018/11/22 原因：清空OTC广告缓存
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdvertiseAmountForBuy(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseAmount(AdvertiseControlStatus.PUT_ON_SHELVES, advertiseId, amount);
        return ret > 0 ? true : false;
    }

    /***
      * 修改广告的可用数量
      * @author yangch
      * @time 2018.07.12 17:23 
     * @param advertiseId 广告ID
     * @param amount 取消数量
     */
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true) //add by tansitao 时间： 2018/11/22 原因：清空OTC广告缓存
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdvertiseAmountForCancel(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    /**
     * 修改广告交易中数量
     * @author Zhang Yanjun
     * @time 2018.10.31 18:12
     * @param advertiseId
     * @param amount
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdvertiseAmountForClose(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }


    /***
      * 修改广告的可用数量
      * @author yangch
      * @time 2018.07.12 17:23 
     * @param advertiseId 广告ID
     * @param amount 释放数量
     */
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true) //add by tansitao 时间： 2018/11/22 原因：清空OTC广告缓存
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdvertiseAmountForRelease(long advertiseId, BigDecimal amount) {
        int ret = advertiseDao.updateAdvertiseDealAmount(advertiseId, amount);
        return ret > 0 ? true : false;
    }

    /**
     * 得到出售类型自动下架的广告
     *
     * @param coinId
     * @param marketPrice
     * @return
     */
    public List<Map<String, String>> selectSellAutoOffShelves(long coinId, BigDecimal marketPrice, BigDecimal jyRate) throws SQLException, DataException {
        String sql = "SELECT b.* FROM (SELECT\n" +
                "\ta.*, CAST(\n" +
                "\t\ta.min_limit / (\n" +
                "\t\t\tCASE a.price_type\n" +
                "\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\ta.price\n" +
                "\t\t\tELSE\n" +
                "\t\t\t\tround(\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t(a.premise_rate + 100) / 100 * ?\n" +
                "\t\t\t\t\t),\n" +
                "\t\t\t\t\t2\n" +
                "\t\t\t\t)\n" +
                "\t\t\tEND\n" +
                "\t\t) AS DECIMAL (18, 8)\n" +
                "\t) minWithdrawAmount\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "WHERE\n" +
                "\ta.`status` = 0\n" +
                "AND a.advertise_type = 1\n" +
                "AND a.coin_id = ?) b WHERE b.remain_amount<ROUND(((? + 100) / 100 * b.minWithdrawAmount),8)";
        List<Map<String, String>> list = DB.query(sql, marketPrice, coinId, jyRate);
        return list;
    }

    /**
     * 得到购买类型自动下架的广告
     *
     * @param coinId
     * @param marketPrice
     * @return
     */
    public List<Map<String, String>> selectBuyAutoOffShelves(long coinId, BigDecimal marketPrice) throws SQLException, DataException {
        String sql = "SELECT b.* FROM (SELECT\n" +
                "\ta.*, CAST(\n" +
                "\t\ta.min_limit / (\n" +
                "\t\t\tCASE a.price_type\n" +
                "\t\t\tWHEN 0 THEN\n" +
                "\t\t\t\ta.price\n" +
                "\t\t\tELSE\n" +
                "\t\t\t\tround(\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t(a.premise_rate + 100) / 100 * ?\n" +
                "\t\t\t\t\t),\n" +
                "\t\t\t\t\t2\n" +
                "\t\t\t\t)\n" +
                "\t\t\tEND\n" +
                "\t\t) AS DECIMAL (18, 8)\n" +
                "\t) minWithdrawAmount\n" +
                "FROM\n" +
                "\tadvertise a\n" +
                "WHERE\n" +
                "\ta.`status` = 0\n" +
                "AND a.advertise_type = 0\n" +
                "AND a.coin_id = ?) b WHERE b.remain_amount<b.minWithdrawAmount";
        List<Map<String, String>> list = DB.query(sql, marketPrice, coinId);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoPutOffShelves(Map<String, String> map, OtcCoin otcCoin) throws UnexpectedException {
//        if (map.get("advertise_type").equals(String.valueOf(AdvertiseType.SELL.ordinal()))) {
//            //MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, Long.valueOf(map.get("member_id")));
//            MemberWallet memberWallet = memberWalletService.findCacheByOtcCoinAndMemberId(otcCoin, Long.valueOf(map.get("member_id")));
//            MessageResult result = memberWalletService.thawBalance(memberWallet, new BigDecimal(map.get("remain_amount")));
//            if (result.getCode() != 0) {
//                throw new UnexpectedException(String.format("解冻钱包失败"));
//                //throw new InformationExpiredException("Information Expired");
//            }
//        }
        //add by tansitao 时间： 2018/11/12 原因：优化下架广告
        Advertise advertise = find(Long.valueOf(map.get("id")), Long.valueOf(map.get("member_id")));
        int is = getService().putOffShelves(advertise);
        if (!(is > 0)) {
            throw new UnexpectedException(String.format("自动下架失败"));
            //throw new InformationExpiredException("Information Expired");
        }
    }


    /**
      * 下架广告
      * @author tansitao
      * @time 2018/11/12 10:26 
      */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true) //add by tansitao 时间： 2018/11/20 原因：清空OTC广告缓存
    public int putOffShelves(Advertise advertise) throws UnexpectedException{
        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
            OtcCoin otcCoin = advertise.getCoin();
            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, advertise.getMember().getId());
            MessageResult result = memberWalletService.thawBalance(memberWallet, advertise.getRemainAmount());
            if (result.getCode() != 0) {
                throw new UnexpectedException(msService.getMessage("INSUFFICIENT_BALANCE"));
            }
        }
        int count = advertiseDao.putOffAdvertise(advertise.getId(), advertise.getRemainAmount());
        if (count <= 0) {
            throw new UnexpectedException(msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        } else {
            return count;
        }
    }

    public List<Advertise> getAllPutOnAdvertis(Long memberId) {
        return advertiseDao.findAllByMemberIdAndStatus(memberId, AdvertiseControlStatus.PUT_ON_SHELVES);
    }

    public Page<Advertise> findAll(com.querydsl.core.types.Predicate predicate, Pageable pageable) {
        return advertiseDao.findAll(predicate, pageable);
    }

    /**
      * 上架广告
      * @author tansitao
      * @time 2018/10/26 13:43 
      */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true) //add by tansitao 时间： 2018/11/20 原因：清空OTC广告缓存
    public void putOnShelves(Advertise advertise,RestTemplate restTemplate) throws Exception{
        Member member = advertise.getMember();
        OtcCoin otcCoin = advertise.getCoin();
        //edit by tansitao 时间： 2018/10/25 原因：非商家广告需要扣除手续费
        if(!advertise.getMember().getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED))
        {
            //普通用户开启折扣，并且币种开启折扣， 扣去普通用户需要收取手续费
            if(advertise.getNeedPutonDiscount() == BooleanEnum.IS_TRUE && otcCoin.getGeneralDiscountRate().compareTo(BigDecimal.ZERO) > 0){
                //用户选择使用折扣优惠
                //获取费率币种、折扣币种的当前USDT价格，若是usdt币种，价格默认为1
                BigDecimal source = BigDecimal.ONE;
                BigDecimal target = BigDecimal.ONE;
                PriceUtil priceUtil = new PriceUtil();
                if (!"USDT".equalsIgnoreCase(otcCoin.getGeneralFeeCoinUnit())){
                    source = priceUtil.getPriceByCoin(restTemplate, otcCoin.getGeneralFeeCoinUnit());
                }
                if(!"USDT".equalsIgnoreCase(otcCoin.getGeneralDiscountCoinUnit())){
                    target = priceUtil.getPriceByCoin(restTemplate, otcCoin.getGeneralDiscountCoinUnit());
                }
                log.info("======================手续费币种价格{}----{}=========================", source, otcCoin.getGeneralFeeCoinUnit());
                log.info("======================手续费折扣币种价格{}----{}=========================", target, otcCoin.getGeneralDiscountCoinUnit());
                Assert.isTrue(target.compareTo(BigDecimal.ZERO) > 0,otcCoin.getGeneralDiscountCoinUnit() + msService.getMessage("PRICE_ERROR"));

                //使用优惠折扣,计算折扣后的数量
                BigDecimal discountFreeBefor = PriceUtil.toRate(otcCoin.getGeneralFee(), otcCoin.getGeneralDiscountCoinScale(), source, target);
                BigDecimal discountFreeAfter = discountFreeBefor.multiply(otcCoin.getGeneralDiscountRate()).setScale(otcCoin.getGeneralDiscountCoinScale(), BigDecimal.ROUND_DOWN);
                log.info("======================打折过后的手续费{}{}=========================", discountFreeAfter, otcCoin.getGeneralFeeCoinUnit());

                //操作用户钱包，扣除打折后的手续费,
                BigDecimal realDiscountFree = BigDecimal.ZERO;
                MemberWallet discountMemberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getGeneralDiscountCoinUnit(), member.getId());
                //折扣币种余额足够，扣除折扣币种
                if(discountMemberWallet.getBalance().compareTo(discountFreeAfter) >= 0){
                    realDiscountFree = discountFreeAfter;
                }else
                {
                    //折扣币种余额不足，进行混合扣除
                    realDiscountFree = discountMemberWallet.getBalance();
                    //edit by tansitao 时间： 2018/11/14 原因：修改进度处理
//                    BigDecimal notEnoughFree = otcCoin.getGeneralFee().subtract(realDiscountFree.multiply(target).divide(otcCoin.getGeneralDiscountRate()).setScale(otcCoin.getGeneralDiscountCoinScale(), BigDecimal.ROUND_UP));
                    //add|edit|del by tansitao 时间： 2018/11/26 原因：修复bug，计算规则需要除以之前的价格------手续费-（钱包币种余额*价格/折扣率/源币种价格）
                    BigDecimal notEnoughFree = otcCoin.getGeneralFee().subtract(realDiscountFree.multiply(target).divide(otcCoin.getGeneralDiscountRate(), otcCoin.getGeneralDiscountCoinScale(), BigDecimal.ROUND_UP).divide(source, otcCoin.getCoinScale(), BigDecimal.ROUND_UP));
                    //add by tansitao 时间： 2018/12/11 原因：手续费为0则不记录资金记录
                    if(notEnoughFree.compareTo(BigDecimal.ZERO) > 0){
                        log.info("======================折扣币种余额不足，进行混合扣除{}{}=========================", notEnoughFree, otcCoin.getGeneralFeeCoinUnit());
                        //记录不享受折扣资金交易流水
                        MemberTransaction notEnjoyTransaction = new MemberTransaction();
                        notEnjoyTransaction.setAmount(notEnoughFree);
                        notEnjoyTransaction.setMemberId(member.getId());
                        notEnjoyTransaction.setType(TransactionType.ADVERTISE_FEE);
                        notEnjoyTransaction.setSymbol(otcCoin.getGeneralFeeCoinUnit());
                        notEnjoyTransaction.setRefId(advertise.getId() + "");
                        memberTransactionService.save(notEnjoyTransaction);

                        //扣除用户不享受折扣的币种数量
                        MemberWallet notEnjoyDiscountMemberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getGeneralFeeCoinUnit(), member.getId());
                        MessageResult result = memberWalletService.decreaseBalance(notEnjoyDiscountMemberWallet.getId(), notEnoughFree);
                        if (result.getCode() != 0) {
                            throw new InconsistencyException(otcCoin.getGeneralFeeCoinUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));//edit by tansitao 时间： 2018/5/21 原因：增加国际化
                        }
                    }
                }

                //add by tansitao 时间： 2018/12/11 原因：手续费为0则不记录资金记录
                if(realDiscountFree.compareTo(BigDecimal.ZERO) > 0){
                    //记录享受折扣的资金交易流水
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(realDiscountFree);
                    memberTransaction.setMemberId(member.getId());
                    memberTransaction.setType(TransactionType.ADVERTISE_FEE);
                    memberTransaction.setSymbol(otcCoin.getGeneralDiscountCoinUnit());
                    memberTransaction.setComment("实时价格=" + target + "USDT");
                    memberTransaction.setRefId(advertise.getId() + "");
                    memberTransactionService.save(memberTransaction);

                    //扣除享受折扣的币种数量
                    MessageResult result = memberWalletService.decreaseBalance(discountMemberWallet.getId(), realDiscountFree);
                    if (result.getCode() != 0) {
                        throw new InconsistencyException(otcCoin.getGeneralDiscountCoinUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));//edit by tansitao 时间： 2018/5/21 原因：增加国际化
                    }
                }

            }else{
                //用户不使用折扣优惠

                //add by tansitao 时间： 2018/12/11 原因：手续费为0则不记录资金记录
                if(otcCoin.getGeneralFee().compareTo(BigDecimal.ZERO) > 0){
                    //记录资金交易流水
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(otcCoin.getGeneralFee());
                    memberTransaction.setMemberId(member.getId());
                    memberTransaction.setType(TransactionType.ADVERTISE_FEE);
                    memberTransaction.setSymbol(otcCoin.getGeneralFeeCoinUnit());
                    memberTransaction.setRefId(advertise.getId() + "");
                    memberTransactionService.save(memberTransaction);
                    //操作用户钱包，扣除打折后的手续费
                    MemberWallet discountMemberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getGeneralFeeCoinUnit(), member.getId());
                    MessageResult result = memberWalletService.decreaseBalance(discountMemberWallet.getId(), otcCoin.getGeneralFee());
                    if (result.getCode() != 0) {
                        throw new InconsistencyException(otcCoin.getGeneralFeeCoinUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));//edit by tansitao 时间： 2018/5/21 原因：增加国际化
                    }
                }
            }
        }

        //操作用户用户，进行广告余额操作
        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, member.getId());
            Assert.isTrue(BigDecimalUtils.compare(memberWallet.getBalance(), advertise.getNumber()), msService.getMessage("INSUFFICIENT_BALANCE"));
            Assert.isTrue(advertise.getNumber().compareTo(otcCoin.getSellMinAmount()) >= 0, msService.getMessage("SELL_NUMBER_MIN") + otcCoin.getSellMinAmount());
            MessageResult result = memberWalletService.freezeBalance(memberWallet, advertise.getNumber());
            if (result.getCode() != 0) {
                throw new InconsistencyException(msService.getMessage("INSUFFICIENT_BALANCE"));//edit by tansitao 时间： 2018/5/21 原因：增加国际化
            }
        } else {
            Assert.isTrue(advertise.getNumber().compareTo(otcCoin.getBuyMinAmount()) >= 0, msService.getMessage("BUY_NUMBER_MIN") + otcCoin.getBuyMinAmount());
        }
        advertise.setRemainAmount(advertise.getNumber());
        advertise.setStatus(AdvertiseControlStatus.PUT_ON_SHELVES);
        saveAdvertise(advertise);
    }

    /**
     * 查询排序最大的广告
     * @author Zhang Yanjun
     * @time 2018.12.11 10:37
     * @param
     */
    public Map<String,Object> findOneBySortMax(){
        return  advertiseMapper.findOneBySortMax();
    }

    /**
     * 修改广告状态
     * @author Zhang Yanjun
     * @time 2018.12.17 10:59
     * @param status
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"otcAdvertise"}, allEntries = true)
    public int updateStatus(AdvertiseControlStatus status, long id){
        return advertiseDao.updateStatus(status, new Date(), id);
    }


    public AdvertiseService getService(){
        return SpringContextUtil.getBean(AdvertiseService.class);
    }
}
