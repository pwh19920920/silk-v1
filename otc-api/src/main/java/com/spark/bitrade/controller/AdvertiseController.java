package com.spark.bitrade.controller;


import com.github.pagehelper.PageInfo;
import com.querydsl.core.types.Predicate;
import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.*;
import com.spark.bitrade.exception.InconsistencyException;
import com.spark.bitrade.model.screen.AdvertiseScreen;
import com.spark.bitrade.pagination.SpecialPageData;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.BindingResultUtil;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.sparkframework.sql.DataException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.PayMode.*;
import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.compare;
import static com.spark.bitrade.util.BigDecimalUtils.mulRound;
import static org.springframework.util.Assert.isTrue;


/**
 * @author Zhang Jinwei
 * @date 2017年12月08日
 */
@RestController
@RequestMapping("/advertise")
@Slf4j
public class AdvertiseController extends BaseController {

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private CountryService countryService;
    @Autowired
    private MemberPaymentAccountService memberPaymentAccountService;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ISilkDataDistService iSilkDataDistService;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;
    @Value("${spark.system.advertise:0}")
    private int allow;
    @Autowired
    private OtcOrderService otcOrderService;

    /**
     * 创建广告
     *
     * @param advertise 广告{@link Advertise}
     * @return
     */
    @RequestMapping(value = "create")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult create(@Valid Advertise advertise, BindingResult bindingResult,
                                @SessionAttribute(SESSION_MEMBER) AuthMember member,
                                @RequestParam(value = "pay[]") String[] pay, String jyPassword) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Member member1 = memberService.findOne(member.getId());
        //判断普通用户是否可以发布广告
        if (member1.getMemberLevel() != MemberLevelEnum.IDENTIFICATION
                && this.getAdvertiseConfig() == false) {
            return error(msService.getMessage("CAN_NOT_PUBLISH"));
        }
        //add by tansitao 时间： 2018/11/14 原因：最大交易额限制
        if (advertise.getPriceType() == PriceType.REGULAR && advertise.getAdvertiseType() == AdvertiseType.SELL) {
            BigDecimal maxLimit = advertise.getNumber().multiply(advertise.getPrice());
            Assert.isTrue(maxLimit.compareTo(advertise.getMaxLimit()) >= 0, msService.getMessage("max_trade_limit") + maxLimit);

        }

        OtcCoin otcCoin = otcCoinService.findOne(advertise.getCoin().getId());
        //add by tansitao 时间： 2018/11/12 原因：如果等于cnyt则价格必须唯一
        if ("CNYT".equals(otcCoin.getUnit())) {
            //add by tansitao 时间： 2018/12/14 原因：CNYT不能为溢价
            Assert.isTrue(PriceType.MUTATIVE != advertise.getPriceType(), msService.getMessage("MUST_CHANGE"));
            Assert.isTrue(advertise.getPrice() != null && advertise.getPrice().compareTo(BigDecimal.ONE) == 0, msService.getMessage("MUST_EQ_ONE"));
        }

        Assert.notEmpty(pay, msService.getMessage("MISSING_PAY"));
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));

        //del by tansitao 时间： 2018/11/7 原因：1.3需求降低发布广告的门槛，注释掉布广告的权限判断
//        Assert.isTrue(member1.getIdNumber() != null, msService.getMessage("NO_REALNAME"));
//        if (allow == 1) {
//            //allow是1的时候，必须是认证商家才能发布广告
//            Assert.isTrue(member1.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION), msService.getMessage("NO_BUSINESS"));
//        }
        //add by zyj：publishAdvertise为0时禁止发布广告
        isTrue(StringUtils.isEmpty(member1.getTransactionStatus()) || member1.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
        String message="";
        if(advertise.getAdvertiseType()==AdvertiseType.BUY){
            message= msService.getMessage("NO_ALLOW_TRANSACT_BUY");
        }else {
            message= msService.getMessage("NO_ALLOW_TRANSACT_SELL");
        }
        log.info("广告类型为:{}",advertise.getAdvertiseType().getCnName());
        validateOpenTranscationService.validateOpenExPitTransaction(member.getId(), message,advertise.getAdvertiseType());
        isTrue(StringUtils.isEmpty(member1.getPublishAdvertise()) || member1.getPublishAdvertise() == BooleanEnum.IS_TRUE, msService.getMessage("NOT_ADVERTISING"));

        String mbPassword = member1.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member1.getSalt(), 2).toHex().toLowerCase();
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        AdvertiseType advertiseType = advertise.getAdvertiseType();
        if (advertiseType == AdvertiseType.BUY) {
            advertise.setNumber(BigDecimal.valueOf(1000000L)); //add by tansitao 时间： 2018/11/1 原因：买币广告增加默认数量
            //add by tansitao 时间： 2018/11/6 原因：增加发布广告限制
            if (!member.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION)) {
                MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, member.getId());
                Assert.isTrue(memberWallet.getBalance().compareTo(otcCoin.getGeneralBuyMinBalance()) >= 0, otcCoin.getUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));
            }
        }
        StringBuffer payMode = checkPayMode(pay, advertiseType, member1);
        advertise.setPayMode(payMode.toString());
        checkAmount(advertiseType, advertise, otcCoin, member1);
        advertise.setLevel(AdvertiseLevel.ORDINARY);
        advertise.setRemainAmount(advertise.getNumber());
        Member mb = new Member();
        mb.setId(member.getId());
        advertise.setMember(mb);
        Advertise ad = advertiseService.saveAdvertise(advertise);
        if (ad != null) {
            return MessageResult.success(msService.getMessage("CREATE_SUCCESS"));
        } else {
            return MessageResult.error(msService.getMessage("CREATE_FAILED"));
        }
    }

    /**
     * 个人所有广告
     *
     * @param shiroUser
     * @return
     */
    @RequestMapping(value = "all")
    public MessageResult allNormal(
            @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser) {
        Sort.Order order = new Sort.Order(Sort.Direction.DESC, "id");
        Sort sort = new Sort(order);
        List<MemberAdvertise> list = advertiseService.getAllAdvertiseByMemberId(shiroUser.getId(), sort);
        //add|edit|del by tansitao 时间： 2018/5/19 原因：修改我的广告当价格为溢价时，显示错误的问题
        if (list != null) {
            for (MemberAdvertise memberAdvertise : list) {
                if (memberAdvertise != null) {
                    if (PriceType.MUTATIVE == memberAdvertise.getPriceType()) {
                        OtcCoin otcCoin = otcCoinService.findByUnit(memberAdvertise.getCoinUnit());
                        BigDecimal marketPrice = coins.get(otcCoin.getUnit());//获取币种价格
                        BigDecimal premiseRate = memberAdvertise.getPremiseRate().divide(new BigDecimal(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
                        if (memberAdvertise.getAdvertiseType() == AdvertiseType.SELL) {
                            BigDecimal price = mulRound(BigDecimal.ONE.add(premiseRate), marketPrice, 2);
                            memberAdvertise.setPrice(price);
                        } else {
                            BigDecimal price = mulRound(BigDecimal.ONE.subtract(premiseRate), marketPrice, 2);
                            memberAdvertise.setPrice(price);
                        }
                    }
                }

            }
        }
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(list);
        return messageResult;
    }

    /**
     * 个人所有广告
     *
     * @param shiroUser
     * @return
     */
    @RequestMapping(value = "self/all")
    //edit by yangch 时间： 2018.04.29 原因：合并
    /*public MessageResult self(
            PageModel pageModel,
            @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser) {
        BooleanExpression eq = QAdvertise.advertise.member.id.eq(shiroUser.getId());
        Page<Advertise> all = advertiseService.findAll(eq, pageModel.getPageable());
        return success(all);
    }*/
    public MessageResult self(
            AdvertiseScreen screen,
            PageModel pageModel,
            @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser) {
        //添加 指定用户条件
        Predicate predicate = screen.getPredicate(QAdvertise.advertise.member.id.eq(shiroUser.getId()));
        Page<Advertise> all = advertiseService.findAll(predicate, pageModel.getPageable());
        return success(all);
    }

    /**
     * 广告详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "detail")
    public MessageResult detail(Long id, @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser) {
        MemberAdvertiseDetail advertise = advertiseService.findOne(id, shiroUser.getId());
        advertise.setMarketPrice(coins.get(advertise.getCoinUnit()));
        MessageResult result = MessageResult.success();
        result.setData(advertise);
        return result;
    }

    /**
     * 修改广告
     *
     * @param advertise 广告{@link Advertise}
     * @return {@link MessageResult}
     */
    @RequestMapping(value = "update")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(
            @Valid Advertise advertise,
            BindingResult bindingResult,
            @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser,
            @RequestParam(value = "pay[]") String[] pay, String jyPassword) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        //add by tansitao 时间： 2018/11/14 原因：最大交易额限制
        if (advertise.getPriceType() == PriceType.REGULAR && advertise.getAdvertiseType() == AdvertiseType.SELL) {
            BigDecimal maxLimit = advertise.getNumber().multiply(advertise.getPrice());
            Assert.isTrue(maxLimit.compareTo(advertise.getMaxLimit()) >= 0, msService.getMessage("max_trade_limit") + maxLimit);
        }

        Assert.notEmpty(pay, msService.getMessage("MISSING_PAY"));
        Assert.notNull(advertise.getId(), msService.getMessage("UPDATE_FAILED"));
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));

        OtcCoin newotcCoin = otcCoinService.findOne(advertise.getCoin().getId());
        //add by tansitao 时间： 2018/11/12 原因：如果等于cnyt则价格必须唯一
        if ("CNYT".equals(newotcCoin.getUnit())) {
            //add by tansitao 时间： 2018/12/14 原因：CNYT不能为溢价
            Assert.isTrue(PriceType.MUTATIVE != advertise.getPriceType(), msService.getMessage("MUST_CHANGE"));
            Assert.isTrue(advertise.getPrice() != null && advertise.getPrice().compareTo(BigDecimal.ONE) == 0, msService.getMessage("MUST_EQ_ONE"));
        }
        Member member = memberService.findOne(shiroUser.getId());
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        Assert.isTrue(jyPass.equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        AdvertiseType advertiseType = advertise.getAdvertiseType();
        if (advertiseType == AdvertiseType.BUY) {
            advertise.setNumber(BigDecimal.valueOf(1000000L)); //add by tansitao 时间： 2018/11/1 原因：买币广告增加默认数量
            //add by tansitao 时间： 2018/11/6 原因：增加发布广告限制
            if (!member.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION)) {
                MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(newotcCoin, member.getId());
                Assert.isTrue(memberWallet.getBalance().compareTo(newotcCoin.getGeneralBuyMinBalance()) >= 0, newotcCoin.getUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));
            }
        }

        StringBuffer payMode = checkPayMode(pay, advertiseType, member);

        advertise.setPayMode(payMode.toString());
        Advertise old = advertiseService.findOne(advertise.getId());
        Assert.notNull(old, msService.getMessage("UPDATE_FAILED"));
        Assert.isTrue(old.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES), msService.getMessage("AFTER_OFF_SHELVES"));
        OtcCoin otcCoin = otcCoinService.findOne(old.getCoin().getId());
        checkAmount(old.getAdvertiseType(), advertise, otcCoin, member);

//        Country country = countryService.findOne(advertise.getCountry().getZhName());
//        old.setCountry(country);
        Advertise ad = advertiseService.modifyAdvertise(advertise, old);
        if (ad != null) {
            return MessageResult.success(msService.getMessage("UPDATE_SUCCESS"));
        } else {
            return MessageResult.error(msService.getMessage("UPDATE_FAILED"));
        }
    }


    /**
     * 广告上架
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/on/shelves")
    public MessageResult putOnShelves(long id, @SessionAttribute(SESSION_MEMBER) AuthMember authMember) throws Exception {
        Advertise advertise = advertiseService.find(id, authMember.getId());
        Assert.isTrue(advertise != null, msService.getMessage("PUT_ON_SHELVES_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES), msService.getMessage("PUT_ON_SHELVES_FAILED"));

        Member member = memberService.findOne(authMember.getId());

        //判断普通用户是否可以发布广告
        if (member.getMemberLevel() != MemberLevelEnum.IDENTIFICATION
                && this.getAdvertiseConfig() == false) {
            return error(msService.getMessage("CAN_NOT_PUBLISH"));
        }

        //add by tansitao 时间： 2018/11/11 原因：增加广告发布限制
        isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
        String message="";
        if(advertise.getAdvertiseType()==AdvertiseType.BUY){
            message= msService.getMessage("NO_ALLOW_TRANSACT_BUY");
        }else {
            message= msService.getMessage("NO_ALLOW_TRANSACT_SELL");
        }
        validateOpenTranscationService.validateOpenExPitTransaction(member.getId(),
                message,advertise.getAdvertiseType());

        isTrue(StringUtils.isEmpty(member.getPublishAdvertise()) || member.getPublishAdvertise() == BooleanEnum.IS_TRUE, msService.getMessage("NOT_ADVERTISING"));

        //add by tansitao 时间： 2018/12/21 原因：增加广告买币的数量限制
        if (advertise.getAdvertiseType() == AdvertiseType.BUY) {
            advertise.setNumber(BigDecimal.valueOf(1000000L));

            //add by  shenzucai 时间： 2019.06.12  原因：上架广告时也需要检测余额是否足够
            if (!member.getMemberLevel().equals(MemberLevelEnum.IDENTIFICATION)) {
                OtcCoin otcCoin = otcCoinService.findOne(advertise.getCoin().getId());
                MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, member.getId());
                Assert.isTrue(memberWallet.getBalance().compareTo(otcCoin.getGeneralBuyMinBalance()) >= 0, otcCoin.getUnit() + msService.getMessage("INSUFFICIENT_BALANCE"));
            }
        }

        //edit by tansitao 时间： 2018/10/25 原因：非商家广告需要扣除手续费
        advertiseService.putOnShelves(advertise, restTemplate);
        return MessageResult.success(msService.getMessage("PUT_ON_SHELVES_SUCCESS"));
    }

    /**
     * 广告下架
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/off/shelves")
//    @Transactional(rollbackFor = Exception.class) //del by tansitao 时间： 2018/11/12 原因：取消事务
    public MessageResult putOffShelves(long id, @SessionAttribute(SESSION_MEMBER) AuthMember authMember) throws Exception {
        Advertise advertise = advertiseService.find(id, authMember.getId());
        Assert.isTrue(advertise != null, msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        //add by tansitao 时间： 2018/6/9 原因：判断用户是否为商家状态
//        Assert.isTrue(advertise.getMember().getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED) , msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("PUT_OFF_SHELVES_FAILED"));
//        OtcCoin otcCoin = advertise.getCoin();
//        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
//            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, authMember.getId());
//            MessageResult result = memberWalletService.thawBalance(memberWallet, advertise.getRemainAmount());
//            if (result.getCode() != 0) {
//                throw new InconsistencyException(msService.getMessage("INSUFFICIENT_BALANCE"));
//            }
//        }
        //edit by tansitao 时间： 2018/11/12 原因：修改下架广告逻辑
        int ret = advertiseService.putOffShelves(advertise);
        if (!(ret > 0)) {
            throw new InconsistencyException(msService.getMessage("PUT_OFF_SHELVES_FAILED"));
        }
       /* advertise.setNumber(BigDecimal.ZERO);
        advertise.setRemainAmount(BigDecimal.ZERO);
        advertise.setStatus(AdvertiseControlStatus.PUT_OFF_SHELVES);*/
        return MessageResult.success(msService.getMessage("PUT_OFF_SHELVES_SUCCESS"));
    }


    /**
     * 删除广告
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "delete")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult delete(Long id, @SessionAttribute(SESSION_MEMBER) AuthMember shiroUser) {
        Advertise advertise = advertiseService.find(id, shiroUser.getId());
        Assert.notNull(advertise, msService.getMessage("DELETE_ADVERTISE_FAILED"));
        //add by tansitao 时间： 2018/11/12 原因：取消判断用户是否为商家状态
//        Assert.isTrue(advertise.getMember().getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED) , msService.getMessage("DELETE_ADVERTISE_FAILED"));
        Assert.isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_OFF_SHELVES), msService.getMessage("DELETE_AFTER_OFF_SHELVES"));
        advertise.setStatus(AdvertiseControlStatus.TURNOFF);
        return MessageResult.success(msService.getMessage("DELETE_ADVERTISE_SUCCESS"));
    }


    /**
     * 查询优质广告
     *
     * @return
     */
    @RequestMapping(value = "excellent")
    public MessageResult allExcellentAdvertise(AdvertiseType advertiseType) throws Exception {
        List<Map<String, String>> marketPrices = new ArrayList<>();
        List<Map<String, String>> otcCoins = otcCoinService.getAllNormalCoin();
        otcCoins.stream().forEachOrdered(x -> {
            Map<String, String> map = new HashMap<>(2);
            map.put("name", x.get("unit"));
            map.put("price", coins.get(x.get("unit")).toString());
            marketPrices.add(map);
        });
        List<ScanAdvertise> list = advertiseService.getAllExcellentAdvertise(advertiseType, marketPrices);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(list);
        return messageResult;
    }

    /**
     * 分页查询广告
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "page")
    public MessageResult queryPageAdvertise(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                            @RequestParam Long id, @RequestParam AdvertiseType advertiseType,
                                            @RequestParam(value = "isCertified", defaultValue = "0") Integer isCertified) throws SQLException, DataException {
        OtcCoin otcCoin = otcCoinService.findOne(id);
        double marketPrice = coins.get(otcCoin.getUnit()).doubleValue();
//        SpecialPage<ScanAdvertise> page = advertiseService.paginationAdvertise(pageNo, pageSize, otcCoin, advertiseType, marketPrice, isCertified);
        //edit by tansitao 时间： 2018/7/17 原因：修改为从只读数据库里面查询分页信息
        PageInfo<OtcAdvertise> page = advertiseService.pageAdvertise(SpecialPageData.pageNo4PageHelper(pageNo), pageSize, id, advertiseType.getOrdinal(), AdvertiseControlStatus.PUT_ON_SHELVES.getOrdinal(), marketPrice, otcCoin.getCoinScale());
        page.getList().forEach(otcAdvertise -> {
            //获取币种信息
            otcAdvertise.setCoinName(otcCoin.getName());
            otcAdvertise.setUnit(otcCoin.getUnit());
            otcAdvertise.setCoinNameCn(otcCoin.getNameCn());
            otcAdvertise.setCountry(countryService.findOne(otcAdvertise.getCountryName()));
        });
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(SpecialPageData.toPageData(page));
        return messageResult;
    }

    @RequestMapping(value = "page-by-unit")
    public MessageResult queryPageAdvertiseByUnit(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                  String unit, AdvertiseType advertiseType, AdvertiseRankType advertiseRankType, BooleanEnum isPositive) throws SQLException, DataException {
        OtcCoin otcCoin = otcCoinService.findByUnit(unit);
        Assert.notNull(otcCoin, "validate otcCoin unit!");
        double marketPrice = coins.get(otcCoin.getUnit()).doubleValue();
        //add tansitao 时间： 2018/8/27 原因：增加订单主动排序
        PageInfo<OtcAdvertise> page = null;
        if (advertiseRankType != null) {
            //add by tansitao 时间： 2018/10/26 原因：添加精度
            page = advertiseService.pageAdvertiseRank(pageNo, pageSize, otcCoin.getId(), advertiseType.getOrdinal(), AdvertiseControlStatus.PUT_ON_SHELVES.getOrdinal(), marketPrice, advertiseRankType, isPositive, otcCoin.getCoinScale());
        } else {
            //add by tansitao 时间： 2018/10/26 原因：添加精度
            page = advertiseService.pageAdvertise(pageNo, pageSize, otcCoin.getId(), advertiseType.getOrdinal(), AdvertiseControlStatus.PUT_ON_SHELVES.getOrdinal(), marketPrice, otcCoin.getCoinScale());
        }
        page.getList().forEach(otcAdvertise -> {
            //获取币种信息
            otcAdvertise.setCoinName(otcCoin.getName());
            otcAdvertise.setUnit(otcCoin.getUnit());
            otcAdvertise.setCoinNameCn(otcCoin.getNameCn());
            otcAdvertise.setCountry(countryService.findOne(otcAdvertise.getCountryName()));
            //add by tansitao 时间： 2018/11/20 原因：优化，计算溢价时广告的价格
            if (otcAdvertise.getPriceType() == PriceType.MUTATIVE) {
                BigDecimal premiseRate = otcAdvertise.getPremiseRate().divide(BigDecimal.valueOf(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
                if (otcAdvertise.getAdvertiseType() == AdvertiseType.SELL) {
                    premiseRate = premiseRate.add(BigDecimal.ONE);
                } else if (otcAdvertise.getAdvertiseType() == AdvertiseType.BUY) {
                    premiseRate = BigDecimal.ONE.subtract(premiseRate);
                }
                otcAdvertise.setPrice(premiseRate.multiply(BigDecimal.valueOf(marketPrice)));
            }
            //add by tansitao 时间： 2018/11/20 原因：从redis中获取交易中的订单数
            Integer onlineNum = (Integer) redisService.getHash(SysConstant.C2C_MONITOR_ORDER + otcAdvertise.getMemberId() + "-" + otcAdvertise.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
            otcAdvertise.setTradingOrderNume(onlineNum == null ? 0 : onlineNum);
        });

        List<OtcAdvertise> otcAdvertises = doSort(page.getList(), advertiseType,advertiseRankType);
        page.setList(otcAdvertises);
        MessageResult messageResult = MessageResult.success();
        messageResult.setData(SpecialPageData.toPageData(page));
        return messageResult;
    }

    /**
     * 新增排序规则
     * 1、正在进行中的订单少的
     * 2、48小时內接单次数少的
     * 3、48小时內接单金额小的
     */
    private List<OtcAdvertise> doSort(List<OtcAdvertise> advertises, AdvertiseType type,AdvertiseRankType advertiseRankType) {
        Set<Long> sids = advertises.stream().map(a -> a.getMemberId()).collect(Collectors.toSet());
        List<MemberOrderCount> memberOrderCounts = new ArrayList<>();
        advertises.forEach(aa->memberOrderCounts.add(new MemberOrderCount().setMemberId(aa.getMemberId())
        .setPrice(aa.getPrice()).setHasTrade(aa.getTransactions()).setAdverId(aa.getAdvertiseId()).setSort(aa.getSort())));
        //正在进行中订单数
        List<Map<String, Long>> tradings = otcOrderService.selectCountByMembers(sids.toArray(new Long[0]), type);
        //商家48小时的接单数 和金额
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        List<Map<String, Long>> count48 = otcOrderService.selectCountByMembersAnd48(sids.toArray(new Long[0]), type, calendar.getTime());
        for (MemberOrderCount count : memberOrderCounts) {
            Long memberId = count.getMemberId();
            for (Map<String, Long> map : tradings) {
                Long m = map.get("member_id");
                if (memberId.equals(m)) {
                    count.setTradingCounts(map.get("count"));
                }
            }

            for (Map<String, Long> mp : count48) {
                Long m = mp.get("member_id");
                if (memberId.equals(m)) {
                    Long count1 = mp.get("count");
                    count.setCount48(count1);
                    Object money = mp.get("_money");
                    count.setMoney48(new BigDecimal(String.valueOf(money)));
                }
            }

            Long tradingCounts = count.getTradingCounts();
            count.setTradingCounts(tradingCounts==null?0L:tradingCounts);
            Long count481 = count.getCount48();
            count.setCount48(count481==null?0L:count481);
            BigDecimal money48 = count.getMoney48();
            count.setMoney48(money48==null?BigDecimal.ZERO:money48);

        }

        Comparator comparator=new OrderComparator(advertiseRankType,type);
        memberOrderCounts.sort(comparator);
        List<OtcAdvertise> ads=new ArrayList<>();
        for (MemberOrderCount c:memberOrderCounts){
            Long adId = c.getAdverId();
            Iterator<OtcAdvertise> iterator = advertises.iterator();
            while (iterator.hasNext()){
                OtcAdvertise next = iterator.next();
                Long m2 = next.getAdvertiseId();
                if(adId.equals(m2)){
                    ads.add(next);
                    iterator.remove();
                }
            }
        }

        return ads;
    }


    @RequestMapping(value = "member", method = RequestMethod.POST)
    public MessageResult memberAdvertises(String name) {
        Member member = memberService.findByUsername(name);
        if (member != null) {

            MemberAdvertiseInfo memberAdvertise = advertiseService.getMemberAdvertise(member, coins.getCoins());
            // TODO 临时过滤BT和DCC币种
            List<String> exclude = Collections.singletonList("DCC");
            List<ScanAdvertise> buy = memberAdvertise.getBuy().stream().filter(i -> !exclude.contains(i.getUnit())).collect(Collectors.toList());
            List<ScanAdvertise> sell = memberAdvertise.getSell().stream().filter(i -> !exclude.contains(i.getUnit())).collect(Collectors.toList());
            memberAdvertise.setBuy(buy);
            memberAdvertise.setSell(sell);
            MessageResult result = MessageResult.success();
            result.setData(memberAdvertise);
            return result;
        } else {
            return MessageResult.error(msService.getMessage("MEMBER_NOT_EXISTS"));
        }
    }

    private StringBuffer checkPayMode(String[] pay, AdvertiseType advertiseType, Member member) {
        StringBuffer payMode = new StringBuffer();
        Arrays.stream(pay).forEach(x -> {
            if (advertiseType.equals(AdvertiseType.SELL)) {
                if (ALI.getCnName().equals(x)) {
                    Assert.isTrue(member.getAlipay() != null, msService.getMessage("NO_ALI"));
                } else if (WECHAT.getCnName().equals(x)) {
                    Assert.isTrue(member.getWechatPay() != null, msService.getMessage("NO_WECHAT"));
                } else if (BANK.getCnName().equals(x)) {
                    Assert.isTrue(member.getBankInfo() != null, msService.getMessage("NO_BANK"));
                } else if (EPAY.getCnName().equals(x)) {
                    //add by tansitao 时间： 2018/8/14 原因：增加epay支付
                    MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
                    Assert.isTrue(memberPaymentAccount != null, msService.getMessage("NO_EPAY"));
                } else {
                    throw new IllegalArgumentException("pay parameter error");
                }
            }
            payMode.append(x + ",");
        });
        return payMode.deleteCharAt(payMode.length() - 1);
    }

    private void checkAmount(AdvertiseType advertiseType, Advertise advertise, OtcCoin otcCoin, Member member) {
        if (advertiseType.equals(AdvertiseType.SELL)) {
            Assert.isTrue(compare(advertise.getNumber(), otcCoin.getSellMinAmount()), msService.getMessage("SELL_NUMBER_MIN") + otcCoin.getSellMinAmount());
            MemberWallet memberWallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, member.getId());
            Assert.isTrue(compare(memberWallet.getBalance(), advertise.getNumber()), msService.getMessage("INSUFFICIENT_BALANCE"));
        } else {
            Assert.isTrue(compare(advertise.getNumber(), otcCoin.getBuyMinAmount()), msService.getMessage("BUY_NUMBER_MIN") + otcCoin.getBuyMinAmount());
        }
    }


    /**
     * 查询普通用户是否可以发布广告（false为不可）
     *
     * @author Zhang Yanjun
     * @time 2019.02.27 11:24
     */
    @PostMapping("/advertiseConfig")
    public MessageRespResult advertiseConfig() {
        boolean isCan = this.getAdvertiseConfig();
        return MessageRespResult.success("查询成功", isCan);
    }

    /**
     * 查询普通用户是否可以发布广告 （false为不可）
     */
    private boolean getAdvertiseConfig() {
        SilkDataDist silkDataDist = iSilkDataDistService.findByIdAndKey("ad_config", "normal_user_publish");
        return iSilkDataDistService.toBoolean(silkDataDist);
    }
}
