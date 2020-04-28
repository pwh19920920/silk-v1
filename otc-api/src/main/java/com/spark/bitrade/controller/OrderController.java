package com.spark.bitrade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.spark.bitrade.annotation.CollectActionEvent;
import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.config.AliyunConfig;
import com.spark.bitrade.config.TradeConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.MonitorRuleConfigDto;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.event.OrderEvent;
import com.spark.bitrade.exception.InformationExpiredException;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.feign.IBTOpenApiService;
import com.spark.bitrade.feign.IOtcServerV2Service;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MyOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.BooleanEnum.IS_FALSE;
import static com.spark.bitrade.constant.BooleanEnum.IS_TRUE;
import static com.spark.bitrade.constant.PayMode.*;
import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.*;
import static com.spark.bitrade.util.MessageResult.error;
import static com.spark.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author Zhang Jinwei
 * @date 2017年12月11日
 */
@Api(description = "C2C订单管理")
@RestController
@RequestMapping(value = "/order", method = RequestMethod.POST)
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdvertiseService advertiseService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private CoinExchangeFactory coins;

    @Autowired
    private OrderEvent orderEvent;

    @Autowired
    private AppealService appealService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderDetailAggregationService orderDetailAggregationService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private MemberPaymentAccountService memberPaymentAccountService;

    @Autowired
    private BusinessDiscountRuleService businessDiscountRuleService;

    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private TradeConfig tradeConfig;

    @Autowired
    private PushOrderMessageService pushOrderMessageService;

    @Autowired
    private OtcCoinService otcCoinService;

    @Autowired
    private AdminOrderAppealSuccessAccessoryService adminOrderAppealSuccessAccessoryService;

    @Autowired
    private MonitorRuleService monitorRuleService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisCountorService redisCountorService;

    @Autowired
    private ITradeCashEvent iTradeCashEvent;

    @Autowired
    private OtcOrderService otcOrderService;

    @Autowired
    private ClickBusinessConfigService clickBusinessConfigService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;

    @Autowired
    IBTOpenApiService ibtOpenApiService;
    @Resource
    IOtcServerV2Service iOtcServerV2Service;

    /**
     * 买入，卖出详细信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "pre", method = RequestMethod.POST)
//    @Transactional(rollbackFor = Exception.class)
    public MessageResult preOrderInfo(@SessionAttribute(SESSION_MEMBER) AuthMember user, long id) {
        Advertise advertise = advertiseService.findOne(id);
        notNull(advertise, msService.getMessage("PARAMETER_ERROR"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("PARAMETER_ERROR"));
        Member member = advertise.getMember();
        Member userMember = memberService.findOne(user.getId());
        OtcCoin otcCoin = advertise.getCoin();
        //add by tansitao 时间： 2018/11/20 原因：从redis中获取交易中的订单数
        Integer onlineNum = (Integer) redisService.getHash(SysConstant.C2C_MONITOR_ORDER + member.getId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM);
        //币种服务费率
        BigDecimal serviceRate;
        if ("USDC".equals(otcCoin.getUnit())) {
            //USDC服务费率
            if (advertise.getAdvertiseType() == AdvertiseType.BUY) {
                //购买广告，对应用户出售 USDC用户出售手续费 USDC_SELL_CHARGE
                serviceRate = getUSDCService(1);
            } else {
                //出售广告，对应用户购买 USDC用户购买手续费 USDC_PAY_CHARGE
                serviceRate = getUSDCService(0);
            }
        } else {
            serviceRate = otcCoinService.getServiceRate(otcCoin.getUnit());
        }

        CurrencyManage currencyManage = iOtcServerV2Service.getCurrencyById(advertise.getCurrencyId()).getData();
        PreOrderInfo preOrderInfo = PreOrderInfo.builder()
                .advertiseType(advertise.getAdvertiseType())
//                .country(advertise.getCountry().getZhName())
                .currencyId(currencyManage.getId())
                .currencyName(currencyManage.getName())
                .currencySymbol(currencyManage.getSymbol())
                .currencyUnit(currencyManage.getUnit())
                .emailVerified(member.getEmail() == null ? IS_FALSE : IS_TRUE)
                .idCardVerified(member.getIdNumber() == null ? IS_FALSE : IS_TRUE)
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getRemainAmount())
                .otcCoinId(otcCoin.getId())
                .payMode(advertise.getPayMode())
                .phoneVerified(member.getMobilePhone() == null ? IS_FALSE : IS_TRUE)
                .timeLimit(advertise.getTimeLimit())
                .transactions(member.getTransactions())
                .unit(otcCoin.getUnit())
                .username(member.getUsername())
                .remark(advertise.getRemark())
                .selfTransactions(userMember.getTransactions())//add by tansitao 时间： 2018/10/26 原因：用户自己的交易次数
                .needBindPhone(advertise.getNeedBindPhone())//add by tansitao 时间： 2018/10/26 原因：增加是否需要手机绑定
                .needRealname(advertise.getNeedRealname())//add by tansitao 时间： 2018/10/26 原因：增加是否需要实名认证
                .needTradeTimes(advertise.getNeedTradeTimes())//add by tansitao 时间： 2018/10/26 原因：增加是否需要一定的交易数量
                .coinScale(otcCoin.getCoinScale())//add by tansitao 时间： 2018/10/26 原因：货币的小数精度
                .memberLevel(member.getMemberLevel()) //add by tansitao 时间： 2018/11/2 原因：增加广告主的身份
                .maxTradingOrders(advertise.getMaxTradingOrders())//add by tansitao 时间： 2018/11/20 原因：同时最大处理订单数 (0 = 不限制)
                .tradingOrderNume(onlineNum == null ? 0 : onlineNum)//add by tansitao 时间： 2018/11/20 原因：交易中的订单数
                .serviceRate(serviceRate) // 服务费率
                .build();
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            preOrderInfo.setPrice(advertise.getPrice());
        } else {
            //获取法币价格
            BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(advertise.getCurrencyId(), otcCoin.getUnit()).getData();
            BigDecimal premiseRate = advertise.getPremiseRate().divide(new BigDecimal(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
            if (advertise.getAdvertiseType() == AdvertiseType.SELL) {
                premiseRate = BigDecimal.ONE.add(premiseRate);
            } else {
                premiseRate = BigDecimal.ONE.subtract(premiseRate);
            }
            BigDecimal price = mulRound(premiseRate, marketPrice, otcCoin.getCoinScale());
            preOrderInfo.setPrice(price); //edit by tansitao 时间： 2018/11/11 原因：修改币种精度
        }
        MessageResult result = MessageResult.success();
        result.setData(preOrderInfo);
        return result;
    }

    /**
     * 买币
     *
     * @param id
     * @param coinId
     * @param price
     * @param money
     * @param amount
     * @param remark
     * @param user
     * @return
     * @throws UnexpectedException
     */
    @RequestMapping(value = "buy", method = RequestMethod.POST)
    public MessageResult buy(long id, long coinId, BigDecimal price, BigDecimal money,
                             BigDecimal amount, String remark,
                             @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                             @SessionAttribute(SESSION_MEMBER) AuthMember user, HttpServletRequest request) throws UnexpectedException {
        Member member = memberService.findOne(user.getId());
        //add by tansitao 时间： 2018/5/14 原因：用户买币增加限制
        if (1 == tradeConfig.getIsOpenLimit()) {
            isTrue(orderService.isAllowTrade(user.getId(), tradeConfig.getOrderNum()), msService.getMessage("NO_ALLOW_TRADE"));
//            isTrue(orderService.isAllowTradeByCancelNum(user.getId(), tradeConfig.getOrderCancleNum()), msService.getMessage("NO_ALLOW_TRADE_DAY"));
            isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
            validateOpenTranscationService.validateOpenExPitTransaction(member.getId(), msService.getMessage("NO_ALLOW_TRANSACT_BUY"), AdvertiseType.BUY);
        }

        //edit by tansitao 时间： 2018/7/17 原因：修改为只读数据库操作
        Advertise advertise = advertiseService.findById(id);
        OtcCoin otcCoin = otcCoinService.findOne(advertise.getCoinId());

        //验证用户默认法币配置跟广告限制是否一致
        MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
        isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
        isTrue(advertise.getCurrencyId().equals(currencyManageMessageRespResult.getData().getId()), msService.getMessage("BASE_CURRENCY_NOT_SUPPORT_OTC_COIN"));

        //del by tansitao 时间： 2018/10/31 原因：取消精度为币种配置的精度
        isTrue(isEqualIgnoreTailPrecision(mulRound(amount, price, 2), money), msService.getMessage("NUMBER_ERROR"));//edit by tansitao 时间： 2018/8/31 原因：修改精度
        //add by tansitao 时间： 2018/10/31 原因：取消对数量和金额、价格的关系判断，直接计算出价格对应的数量
//        amount = money.divide(price, otcCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
        //add by tansitao 时间： 2018/10/26 原因：增加广告的身份证、手机绑定、交易次数等限制判断
        if (advertise.getNeedBindPhone() == BooleanEnum.IS_TRUE) {
            isTrue(!StringUtils.isEmpty(member.getMobilePhone()), msService.getMessage("NOT_BIND_PHONE"));
        }
        if (advertise.getNeedRealname() == BooleanEnum.IS_TRUE) {
            isTrue(!StringUtils.isEmpty(member.getRealName()), msService.getMessage("NO_REAL_NAME"));
        }
        if (advertise.getNeedTradeTimes() > 0) {
            isTrue(member.getTransactions() >= advertise.getNeedTradeTimes(), msService.getMessage("TRANSACTIONS_NOT_ENOUGH"));
        }
        if (advertise.getMaxTradingOrders() > 0) {
            //add by tansitao 时间： 2018/11/19 原因：判断交易中的订单数是否超过配置中的最大订单数
            Integer onlineNum = (Integer) redisService.getHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM);
            isTrue(onlineNum == null || onlineNum < advertise.getMaxTradingOrders(), msService.getMessage("MAX_TRADING_ORDERS"));
        }


        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("ALREADY_PUT_OFF"));
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString());
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString());

        Member bMember = memberService.findOne(advertise.getBMemberId());
        // by wsy, date: 2019-1-23 14:48:52，reason: 限制商家与商家交易， 卖家和买家角色都需要判断
        isTrue(bMember.getMemberLevel() != MemberLevelEnum.IDENTIFICATION || member.getMemberLevel() != MemberLevelEnum.IDENTIFICATION, msService.getMessage("SELLER_ALLOW_TRADE"));

        isTrue(!user.getName().equals(bMember.getUsername()), msService.getMessage("NOT_ALLOW_BUY_BY_SELF"));

        advertise.setMember(bMember);

        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(advertise.getCurrencyId(), otcCoin.getUnit()).getData();
            BigDecimal premiseRate = advertise.getPremiseRate().divide(new BigDecimal(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
            if (advertise.getAdvertiseType() == AdvertiseType.SELL) {
                premiseRate = BigDecimal.ONE.add(premiseRate);
            } else {
                premiseRate = BigDecimal.ONE.subtract(premiseRate);
            }
            BigDecimal _price = mulRound(premiseRate, marketPrice, otcCoin.getCoinScale());
            //edit by tansitao 时间： 2018/10/26 原因：修改精度为配置的精度
            isTrue(isEqual(price, _price), msService.getMessage("PRICE_EXPIRED"));
        }


        //计算手续费
        BigDecimal commission;
        if ("USDC".equals(otcCoin.getUnit())) {
            commission = getCommission(amount, 0);
        } else {
            commission = mulRound(amount, getRate(otcCoin.getJyRate()));
            //手续费折扣率
            BigDecimal feeDiscount = BigDecimal.ZERO;
            if (otcCoin.getFeeSellDiscount().compareTo(BigDecimal.ONE) >= 0) {
                feeDiscount = commission;
            } else {
                feeDiscount = commission.multiply(otcCoin.getFeeSellDiscount());
                BigDecimal remainingFee = commission.subtract(feeDiscount); //优惠后的当前手续费
                //计算 当前会员可优惠手续费数量
                BigDecimal memberFeeDiscount = remainingFee.multiply(
                        businessDiscountRuleService.getDiscountRule(bMember.getId(),
                                otcCoin.getUnit()).getSellDiscount());

                feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
            }
            commission = commission.subtract(feeDiscount);
        }
        //edit by yangch 时间： 2018.07.31 原因：优化事务
        BigDecimal buyAmount = add(commission, amount);
        //isTrue(compare(advertise.getRemainAmount(), add(commission, amount)), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        isTrue(compare(advertise.getRemainAmount(), buyAmount), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        /*if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), add(commission, amount))) {
            throw new UnexpectedException(String.format("OTCGM001：%s", msService.getMessage("CREATE_ORDER_FAILED")));
        }*/


        //add by zyj 2018.12.27 : 接入风控
        TradeCashInfo tradeCashInfo = new TradeCashInfo();
        tradeCashInfo.setDirection(0);
        tradeCashInfo.setCoin(otcCoin.getUnit());
        tradeCashInfo.setAmount(amount);
        tradeCashInfo.setTargetUser(memberService.findOne(advertise.getBMemberId()));

        MessageResult res = risk(request, null, member, tradeCashInfo);
        if (res.getCode() != 0) {
            return error(res.getMessage());
        }

        //add by tansitao 时间： 2018/11/9 原因：增加卖家和买家的账户姓名处理，有账户的用账户姓名，没有的用真实姓名
        MemberPaymentAccount cmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        MemberPaymentAccount bmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
        String caccountName = cmemberPaymentAccount == null || StringUtils.isEmpty(cmemberPaymentAccount.getAccountName())
                ? member.getRealName() : cmemberPaymentAccount.getAccountName();
        String baccountName = bmemberPaymentAccount == null || StringUtils.isEmpty(bmemberPaymentAccount.getAccountName())
                ? bMember.getRealName() : bmemberPaymentAccount.getAccountName();

        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(1000, 9999));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        //add by ss 时间：2020/03/24 原因：新增法币
        order.setCurrencyId(advertise.getCurrencyId());
        order.setCountry(advertise.getCountryName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(caccountName);
        order.setMemberId(bMember.getId());
        order.setMemberName(bMember.getUsername());
        order.setMemberRealName(baccountName);
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setPrice(price);
        order.setRemark(remark);
        order.setTimeLimit(advertise.getTimeLimit());
        order.setPayCode(randomCode);

        // 买币服务费默认填充
        order.setOrderMoney(order.getMoney());
        order.setServiceRate(new BigDecimal(0));
        order.setServiceMoney(new BigDecimal(0));
//del by ss 时间：2020/03/24 原因：支付方式不再保存到订单
//        String[] pay = advertise.getPayMode().split(",");
//        Arrays.stream(pay).forEach(x -> {
//            if (ALI.getCnName().equals(x)) {
//                order.setAlipay(bMember.getAlipay());
//            } else if (WECHAT.getCnName().equals(x)) {
//                order.setWechatPay(bMember.getWechatPay());
//            } else if (BANK.getCnName().equals(x)) {
//                order.setBankInfo(bMember.getBankInfo());
//            } else if (EPAY.getCnName().equals(x)) {
//                MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
//                if (memberPaymentAccount != null) {
//                    Epay epay = new Epay();
//                    epay.setEpayNo(memberPaymentAccount.getEpayNo());
//                    order.setEpay(epay);
//                }
//            }
//        });

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        //Order order1 = orderService.saveOrder(order);
        Order order1 = orderService.buyOrder(order, advertise, buyAmount);
        if (order1 != null) {
            /**
             * 下单后，将自动回复记录添加到mongodb
             */
            if (advertise.getAuto() == BooleanEnum.IS_TRUE && !StringUtils.isEmpty(advertise.getAutoword())) {
                //edit by yangch 时间： 2018.07.12 原因：修改为异步推送自动回复内容
                pushOrderMessageService.pushAutoResponseMessage2Mongodb(advertise, order1);
            }

            //edit by zhongxj 统一在kafka事件消费的时候，推送不同渠道的消息 20190929
//            pushOrderMessageService.pushCreateOrderMessage4SMS(advertise, order1, user);

            //add by tansitao 时间： 2018/11/19 原因：下单成功，redis中的订单数加一
            redisCountorService.addOrSubtractHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM, 1L);

            getService().creatOrderEvent(order1, user);
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCGM002：%s", msService.getMessage("CREATE_ORDER_FAILED")));
        }
    }

    /**
     * 获取USDC用户手续费
     *
     * @param amount 金额
     * @param type   0用户购买 1用户出售
     * @return
     */
    private BigDecimal getCommission(BigDecimal amount, Integer type) {
        //USDC 处理
        return amount.multiply(getUSDCService(type));
    }


    /**
     *  * 一键买币
     *  * @author tansitao
     *  * @time 2019/1/3 14:10 
     *  
     */
    @ApiOperation(value = "一键买币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinUnit", value = "币种"),
            @ApiImplicitParam(name = "amount", value = "数量"),
            @ApiImplicitParam(name = "money", value = "金额"),
            @ApiImplicitParam(name = "pays", value = "支付方式"),
            @ApiImplicitParam(name = "currencyId", value = "法币币种")
    })
    @RequestMapping(value = "aKeyBuy", method = RequestMethod.POST)
    public MessageResult aKeyBuy(String coinUnit, BigDecimal amount, BigDecimal money, String[] pays,
                                 @SessionAttribute(SESSION_MEMBER) AuthMember user, HttpServletRequest request,
                                 @RequestParam(defaultValue = "0") Integer buyType, Long currencyId) throws UnexpectedException {
        // isTrue("CNYT".equals(coinUnit), msService.getMessage("PRICE_EXPIRED"));
        Assert.notEmpty(pays, msService.getMessage("MISSING_PAY"));
        Member member = memberService.findOne(user.getId());
        // OrderSourceType sourceType = OrderSourceType.convert(user.getPlatform());

        // add by wsy, date: 2019-1-23 14:48:52，reason: 限制商家与商家交易， 只需要判断买家角色
        // isTrue(member.getMemberLevel() != MemberLevelEnum.IDENTIFICATION, msService.getMessage("SELLER_ALLOW_TRADE"));

        // add by wsy, date: 2019-1-23 14:48:52，reason: 限制买币用户必须实名认证
        // isTrue(member.getRealNameStatus() == RealNameStatus.VERIFIED, msService.getMessage("NO_REAL_NAME"));

        //获取币种信息
        OtcCoin otcCoin = otcCoinService.findByUnit(coinUnit);
        //验证用户默认法币配置跟限制是否一致 DCC不验证
        if(!"DCC".equalsIgnoreCase(coinUnit)){
            MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
            isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
            isTrue(currencyId.equals(currencyManageMessageRespResult.getData().getId()), msService.getMessage("BASE_CURRENCY_NOT_SUPPORT_OTC_COIN"));
        }else{
            //默认CNY
            currencyId = 1L;
        }

        isTrue(otcCoin != null, msService.getMessage("COIN_ILLEGAL"));

        //add by tansitao 时间： 2018/5/14 原因：用户买币增加限制
        if (1 == tradeConfig.getIsOpenLimit()) {
            isTrue(orderService.isAllowTrade(user.getId(), tradeConfig.getOrderNum()), msService.getMessage("NO_ALLOW_TRADE"));
            isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
            validateOpenTranscationService.validateOpenExPitTransaction(member.getId(), msService.getMessage("NO_ALLOW_TRANSACT_BUY"), AdvertiseType.BUY);
        }

        //add by  shenzucai 时间： 2019.04.29  原因：修复适配到浮动定价的广告没有正确记录价格的bug
        //获取法币价格
        BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(currencyId, coinUnit).getData();

        //获取支持一键购买的商家
//        List<ClickBusinessConfig> clickBusinessConfigList = clickBusinessConfigService.getAllClickBusiness();
//         String appId = sourceType != null ? String.valueOf(sourceType.getOrdinal()) : user.getPlatform();
        //add by  shenzucai 时间： 2019.03.21  原因：使用同一的工具，处理appid
        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());
        List<ClickBusinessConfig> clickBusinessConfigList = clickBusinessConfigService.getAllClickBusiness(coinUnit, appId);
        List<Long> memberIds = new ArrayList<>();
        isTrue(clickBusinessConfigList != null && clickBusinessConfigList.size() > 0, msService.getMessage("NOT_EXIST_CLICK_BUSINESS"));
        //获取商家们的广告
        clickBusinessConfigList.forEach(clickBusiness -> memberIds.add(clickBusiness.getMemberId()));
        List<Advertise> advertiseList;
        if (buyType == 1) {
            //如果是按照金额购买 按照金额筛选广告
            advertiseList = advertiseService.getByMoneyMemberIds(memberIds, money, AdvertiseType.SELL, otcCoin.getId(), currencyId);
        }else{
            advertiseList = advertiseService.getByMemberIds(memberIds, amount, marketPrice, AdvertiseType.SELL, otcCoin.getId(), currencyId);
        }
        isTrue(advertiseList != null && advertiseList.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        //过滤出满足一键购币策略的广告
        List<Advertise> availableAdvList = new ArrayList<>();
        for (Advertise tempAdvertise : advertiseList) {
            //如果是按照金额购买 按照金额计算出数量
            if (buyType == 1) {
                if(tempAdvertise.getPriceType() == PriceType.REGULAR){
                    //固定的价格
                    amount = money.divide(tempAdvertise.getPrice(), 8, RoundingMode.DOWN);
                }else{
                    amount = money.divide(marketPrice, 8, RoundingMode.DOWN);
                }
            }
            //计算手续费
            BigDecimal commission;
            if ("USDC".equals(otcCoin.getUnit())) {
                commission = getCommission(amount, 0);
            } else {
                commission = mulRound(amount, getRate(otcCoin.getJyRate()));
                //手续费折扣率
                BigDecimal feeDiscount = BigDecimal.ZERO;
                if (otcCoin.getFeeSellDiscount().compareTo(BigDecimal.ONE) >= 0) {
                    feeDiscount = commission;
                } else {
                    feeDiscount = commission.multiply(otcCoin.getFeeSellDiscount());
                    BigDecimal remainingFee = commission.subtract(feeDiscount); //优惠后的当前手续费
                    //计算 当前会员可优惠手续费数量
                    BigDecimal memberFeeDiscount = remainingFee.multiply(
                            businessDiscountRuleService.getDiscountRule(tempAdvertise.getBMemberId(),
                                    otcCoin.getUnit()).getSellDiscount());
                    feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
                }
                commission = commission.subtract(feeDiscount);
            }
            BigDecimal buyAmount = add(commission, amount);
            //判断剩余广告数量是否大于购买数量和书续费收了之和
            if (tempAdvertise.getRemainAmount().compareTo(buyAmount) >= 0) {
                tempAdvertise.setCommission(commission);
                //判断广告商是否为自己
                if (user.getId() != tempAdvertise.getBMemberId()) {
                    //判断广告支付方式是否包含用户选择的支付方式
                    for (String pay : pays) {
                        if (tempAdvertise.getPayMode().contains(pay)) {
                            availableAdvList.add(tempAdvertise);
                            break;
                        }
                    }
                }
            }
        }
        //如果没有可用广告抛出无广告提示
        isTrue(availableAdvList.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        // add by wsy data: 2019-1-24 14:10:13 reason: 增加商家订单数排序
        availableAdvList = filterAvailableAdvList(availableAdvList, AdvertiseType.SELL);

        //随机选择一个满足条件的广告
        Advertise advertise = availableAdvList.get(new Random().nextInt(availableAdvList.size()));
        if (buyType == 1) {

            if (advertise.getPriceType() == PriceType.MUTATIVE) {
                BigDecimal price = getRoundPrice(advertise, otcCoin, marketPrice);
                amount = money.divide(price, 8, RoundingMode.DOWN);
            } else {
                amount = money.divide(advertise.getPrice(), 8, RoundingMode.DOWN);
            }

        }
        Member bMember = memberService.findOne(advertise.getBMemberId());
        advertise.setMember(bMember);
        //add by zyj 2018.12.27 : 接入风控
        TradeCashInfo tradeCashInfo = new TradeCashInfo();
        tradeCashInfo.setDirection(0);
        tradeCashInfo.setCoin(otcCoin.getUnit());
        tradeCashInfo.setAmount(amount);
        tradeCashInfo.setTargetUser(memberService.findOne(advertise.getBMemberId()));

        MessageResult res = risk(request, null, member, tradeCashInfo);
        if (res.getCode() != 0) {
            return error(res.getMessage());
        }

        //add by tansitao 时间： 2018/11/9 原因：增加卖家和买家的账户姓名处理，有账户的用账户姓名，没有的用真实姓名
        MemberPaymentAccount cmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        MemberPaymentAccount bmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
        String caccountName = cmemberPaymentAccount == null || StringUtils.isEmpty(cmemberPaymentAccount.getAccountName())
                ? member.getRealName() : cmemberPaymentAccount.getAccountName();
        String baccountName = bmemberPaymentAccount == null || StringUtils.isEmpty(bmemberPaymentAccount.getAccountName())
                ? bMember.getRealName() : bmemberPaymentAccount.getAccountName();

        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(1000, 9999));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(advertise.getCommission());
        //add by ss 时间：2020/03/24 原因：新增法币
        order.setCurrencyId(advertise.getCurrencyId());
        order.setCountry(advertise.getCountryName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(caccountName);
        order.setMemberId(bMember.getId());
        order.setMemberName(bMember.getUsername());
        order.setMemberRealName(baccountName);
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        if (advertise.getPriceType() == PriceType.MUTATIVE) {
            BigDecimal price = getRoundPrice(advertise, otcCoin, marketPrice);
            order.setPrice(price);
            order.setMoney(price.multiply(amount));
        } else {
            order.setPrice(advertise.getPrice());
            order.setMoney(advertise.getPrice().multiply(amount));
        }
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setTimeLimit(advertise.getTimeLimit());
        order.setPayCode(randomCode);

        // 买币服务费默认填充
        order.setOrderMoney(order.getMoney());
        order.setServiceRate(new BigDecimal(0));
        order.setServiceMoney(new BigDecimal(0));

        //判断价格类型
        /*if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            //固定价格直接设置
            order.setPrice(advertise.getPrice());
        } else {
            //溢价则要计算溢价多少
            //BigDecimal marketPrice = coins.get(otcCoin.getUnit());
            BigDecimal price = mulRound(rate(advertise.getPremiseRate()), marketPrice, otcCoin.getCoinScale());
            order.setPrice(price);
        }*/

        order.setOrderSourceType(AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform()));
        order.setIsOneKey(BooleanEnum.IS_TRUE);

//del by ss 时间：2020/03/24 原因：支付方式不再保存到订单
//        String[] pay = advertise.getPayMode().split(",");
//        Arrays.stream(pay).forEach(x -> {
//            if (ALI.getCnName().equals(x)) {
//                order.setAlipay(bMember.getAlipay());
//            } else if (WECHAT.getCnName().equals(x)) {
//                order.setWechatPay(bMember.getWechatPay());
//            } else if (BANK.getCnName().equals(x)) {
//                order.setBankInfo(bMember.getBankInfo());
//            } else if (EPAY.getCnName().equals(x)) {
//                MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
//                if (memberPaymentAccount != null) {
//                    Epay epay = new Epay();
//                    epay.setEpayNo(memberPaymentAccount.getEpayNo());
//                    order.setEpay(epay);
//                }
//            }
//        });

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        Order order1 = orderService.buyOrder(order, advertise, advertise.getCommission().add(amount));
        if (order1 != null) {
            /**
             * 下单后，将自动回复记录添加到mongodb
             */
            if (advertise.getAuto() == BooleanEnum.IS_TRUE && !StringUtils.isEmpty(advertise.getAutoword())) {
                //edit by yangch 时间： 2018.07.12 原因：修改为异步推送自动回复内容
                pushOrderMessageService.pushAutoResponseMessage2Mongodb(advertise, order1);
            }

            //edit by yangch 时间： 2018.07.12 原因：修改为异步发送订单短信 20190929
//            pushOrderMessageService.pushCreateOrderMessage4SMS(advertise, order1, user);

            //add by tansitao 时间： 2018/11/19 原因：下单成功，redis中的订单数加一
            redisCountorService.addOrSubtractHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM, 1L);

            getService().creatOrderEvent(order1, user);
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCGM002：%s", msService.getMessage("CREATE_ORDER_FAILED")));
        }
    }


    private BigDecimal getRoundPrice(Advertise advertise, OtcCoin otcCoin, BigDecimal marketPrice) {
        BigDecimal premiseRate = advertise.getPremiseRate().divide(new BigDecimal(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
        if (advertise.getAdvertiseType() == AdvertiseType.SELL) {
            premiseRate = BigDecimal.ONE.add(premiseRate);
        } else {
            premiseRate = BigDecimal.ONE.subtract(premiseRate);
        }
        BigDecimal price = mulRound(premiseRate, marketPrice, otcCoin.getCoinScale());

        return price;

    }

    /**
     * 卖币
     *
     * @param id
     * @param coinId
     * @param price
     * @param money
     * @param amount
     * @param remark
     * @param user
     * @return
     * @throws UnexpectedException
     */
    @RequestMapping(value = "sell")
    //@Transactional(rollbackFor = Exception.class)
    public MessageResult sell(long id, long coinId, BigDecimal price, BigDecimal money,
                              BigDecimal amount, String remark,
                              @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                              @SessionAttribute(SESSION_MEMBER) AuthMember user, HttpServletRequest request) throws UnexpectedException {
        Member member = memberService.findOne(user.getId());
        memberService.checkRealName(member);
        //add by tansitao 时间： 2018/5/14 原因：用户卖币增加限制
        if (1 == tradeConfig.getIsOpenLimit()) {
            isTrue(orderService.isAllowTrade(user.getId(), tradeConfig.getOrderNum()), msService.getMessage("NO_ALLOW_TRADE"));
//            isTrue(orderService.isAllowTradeByCancelNum(user.getId()), msService.getMessage("NO_ALLOW_TRADE_DAY"));
            //限制卖出
            isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
            validateOpenTranscationService.validateOpenExPitTransaction(member.getId(), msService.getMessage("NO_ALLOW_TRANSACT_SELL"), AdvertiseType.SELL);
        }
        Advertise advertise = advertiseService.findById(id);

//        Assert.isTrue(member.getTransactionStatus()==null || member.getTransactionStatus()== BooleanEnum.IS_TRUE, msService.getMessage("SELL_FALSE"));

        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.BUY)) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }

        //验证用户默认法币配置跟广告限制是否一致
        MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
        isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
        isTrue(advertise.getCurrencyId().equals(currencyManageMessageRespResult.getData().getId()), msService.getMessage("BASE_CURRENCY_NOT_SUPPORT_OTC_COIN"));

        //add by tansitao 时间： 2018/10/26 原因：增加广告的身份证、手机绑定、交易次数等限制判断
        if (advertise.getNeedBindPhone() == BooleanEnum.IS_TRUE) {
            isTrue(!StringUtils.isEmpty(member.getMobilePhone()), msService.getMessage("NOT_BIND_PHONE"));
        }
        if (advertise.getNeedRealname() == BooleanEnum.IS_TRUE) {
            isTrue(!StringUtils.isEmpty(member.getRealName()), msService.getMessage("NO_REAL_NAME"));
        }
        if (advertise.getNeedTradeTimes() > 0) {
            isTrue(member.getTransactions() >= advertise.getNeedTradeTimes(), msService.getMessage("TRANSACTIONS_NOT_ENOUGH"));
        }
        if (advertise.getMaxTradingOrders() > 0) {
            //add by tansitao 时间： 2018/11/19 原因：判断交易中的订单数是否超过配置中的最大订单数
            Integer onlineNum = (Integer) redisService.getHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM);
            isTrue(onlineNum == null || onlineNum < advertise.getMaxTradingOrders(), msService.getMessage("MAX_TRADING_ORDERS"));
        }
        OtcCoin otcCoin = otcCoinService.findOne(advertise.getCoinId());
        // 判断买币手续费率在缓存中是否存在 20190926
        if (otcCoin == null || otcCoin.getBuyJyRate() == null) {
            otcCoin = otcCoinService.getOtcCoin(advertise.getCoinId());
        }

        //del by tansitao 时间： 2018/10/31 原因：取消对数量的判断
        isTrue(isEqualIgnoreTailPrecision(mulRound(amount, price, 2), money), msService.getMessage("NUMBER_ERROR"));//add by tansitao 时间： 2018/9/1 原因：精度问题


        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("ALREADY_PUT_OFF"));
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString());
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString());
        isTrue(compare(advertise.getRemainAmount(), amount), msService.getMessage("AMOUNT_NOT_ENOUGH"));

        Member bMember = memberService.findOne(advertise.getBMemberId());
        // add by wsy, date: 2019-1-23 14:48:52，reason: 限制商家与商家交易， 卖家和买家角色都需要判断
        isTrue(bMember.getMemberLevel() != MemberLevelEnum.IDENTIFICATION || member.getMemberLevel() != MemberLevelEnum.IDENTIFICATION, msService.getMessage("SELLER_ALLOW_TRADE"));

        isTrue(!user.getName().equals(bMember.getUsername()), msService.getMessage("NOT_ALLOW_SELL_BY_SELF"));

        advertise.setMember(bMember);


        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }

        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            //获取法币价格
            BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(advertise.getCurrencyId(), otcCoin.getUnit()).getData();
            BigDecimal premiseRate = advertise.getPremiseRate().divide(new BigDecimal(100), otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
            if (advertise.getAdvertiseType() == AdvertiseType.SELL) {
                premiseRate = BigDecimal.ONE.add(premiseRate);
            } else {
                premiseRate = BigDecimal.ONE.subtract(premiseRate);
            }
            BigDecimal _price = mulRound(premiseRate, marketPrice, otcCoin.getCoinScale());
            isTrue(isEqual(price, _price), msService.getMessage("PRICE_EXPIRED"));
        }

        //add by zyj 2018.12.27 : 接入风控
        TradeCashInfo tradeCashInfo = new TradeCashInfo();
        tradeCashInfo.setDirection(1);
        tradeCashInfo.setCoin(otcCoin.getUnit());
        tradeCashInfo.setAmount(amount);
        tradeCashInfo.setTargetUser(memberService.findOne(advertise.getBMemberId()));

        MessageResult res = risk(request, null, member, tradeCashInfo);
        if (res.getCode() != 0) {
            return error(res.getMessage());
        }

        //计算手续费 20190826修改为买币手续费
        BigDecimal commission;
        if ("USDC".equals(otcCoin.getUnit())) {
            commission = getCommission(amount, 1);
        } else {
            commission = mulRound(amount, getRate(otcCoin.getBuyJyRate()));
            //手续费折扣率
            BigDecimal feeDiscount = BigDecimal.ZERO;
            if (otcCoin.getFeeBuyDiscount().compareTo(BigDecimal.ONE) >= 0) {
                feeDiscount = commission;
            } else {
                feeDiscount = commission.multiply(otcCoin.getFeeBuyDiscount());
                BigDecimal remainingFee = commission.subtract(feeDiscount); //优惠后的当前手续费
                //计算 当前会员可优惠手续费数量
                BigDecimal memberFeeDiscount = remainingFee.multiply(
                        businessDiscountRuleService.getDiscountRule(bMember.getId(),
                                otcCoin.getUnit()).getBuyDiscount());

                feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
            }
            commission = commission.subtract(feeDiscount);
        }

        MemberWallet wallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, user.getId());
        isTrue(compare(wallet.getBalance(), amount), msService.getMessage("INSUFFICIENT_BALANCE"));

        //add by tansitao 时间： 2018/11/9 原因：增加卖家和买家的账户姓名处理，有账户的用账户姓名，没有的用真实姓名
        MemberPaymentAccount cmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        MemberPaymentAccount bmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
        String caccountName = cmemberPaymentAccount == null || StringUtils.isEmpty(cmemberPaymentAccount.getAccountName())
                ? member.getRealName() : cmemberPaymentAccount.getAccountName();
        String baccountName = bmemberPaymentAccount == null || StringUtils.isEmpty(bmemberPaymentAccount.getAccountName())
                ? bMember.getRealName() : bmemberPaymentAccount.getAccountName();

        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(1000, 9999));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        //add by ss 时间：2020/03/24 原因：新增法币
        order.setCurrencyId(advertise.getCurrencyId());
        order.setCountry(advertise.getCountryName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(caccountName);
        order.setMemberId(bMember.getId());
        order.setMemberName(bMember.getUsername());
        order.setMemberRealName(baccountName);
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setPrice(price);
        order.setRemark(remark);
        order.setTimeLimit(advertise.getTimeLimit());
        order.setPayCode(randomCode);

        // add by wsy, 时间：2019-3-21 09:34:56 原因：收取用户服务费给认证商家
        order.setOrderMoney(order.getMoney());
        if (bMember.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED) {
            // 计算用户服务费
            this.serviceMoney(order, order.getMoney());
        }
        if ("USDC".equals(otcCoin.getUnit())) {
            //USDC 需要减去手续费
            order.setMoney(order.getMoney().subtract(order.getCommission().multiply(order.getPrice()).setScale(2, RoundingMode.UP)));
        }
        String[] pay = advertise.getPayMode().split(",");
        MessageResult result = MessageResult.error(msService.getMessage("CREATE_ORDER_SUCCESS"));
        //del by ss 时间：2020/03/24 原因：支付方式不再保存到订单
//        Arrays.stream(pay).forEach(x -> {
//            if (ALI.getCnName().equals(x)) {
//                if (member.getAlipay() != null) {
//                    result.setCode(0);
//                    order.setAlipay(member.getAlipay());
//                }
//            } else if (WECHAT.getCnName().equals(x)) {
//                if (member.getWechatPay() != null) {
//                    result.setCode(0);
//                    order.setWechatPay(member.getWechatPay());
//                }
//            } else if (BANK.getCnName().equals(x)) {
//                if (member.getBankInfo() != null) {
//                    result.setCode(0);
//                    order.setBankInfo(member.getBankInfo());
//                }
//            } else if (EPAY.getCnName().equals(x)) {
//                MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
//                if (memberPaymentAccount != null) {
//                    result.setCode(0);
//                    Epay epay = new Epay();
//                    epay.setEpayNo(memberPaymentAccount.getEpayNo());
//                    order.setEpay(epay);
//                }
//            }
//        });

        //edit by tansitao 时间： 2018/11/10 原因：取消对广告支付方式的控制
        result.setCode(0);
//        isTrue(result.getCode() == 0, msService.getMessage("AT_LEAST_SUPPORT_PAY"));

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        //Order order1 = orderService.saveOrder(order);
        Order order1 = orderService.sellOrder(order, advertise, wallet, amount);
        if (order1 != null) {
            if (advertise.getAuto() == BooleanEnum.IS_TRUE && !StringUtils.isEmpty(advertise.getAutoword())) {
                //edit by yangch 时间： 2018.07.12 原因：修改为异步推送自动回复内容
                pushOrderMessageService.pushAutoResponseMessage2Mongodb(advertise, order1);
            }

            //edit by yangch 时间： 2018.07.12 原因：修改为异步发送订单短信 20190929
//            pushOrderMessageService.pushCreateOrderMessage4SMS(advertise, order1, user);
            //add by tansitao 时间： 2018/11/19 原因：下单成功，redis中的订单数加一
            redisCountorService.addOrSubtractHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM, 1L);

            getService().creatOrderEvent(order1, user);
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCMB003：%s", msService.getMessage("SELL_FAILED")));
        }
    }


    /**
     *  * 一键卖币
     *  * @author tansitao
     *  * @time 2019/1/8 11:19 
     *  
     */
    @ApiOperation(value = "一键卖币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinUnit", value = "币种"),
            @ApiImplicitParam(name = "amount", value = "数量"),
            @ApiImplicitParam(name = "money", value = "金额"),
            @ApiImplicitParam(name = "pays", value = "支付方式"),
            @ApiImplicitParam(name = "currencyId", value = "法币ID")
    })
    @RequestMapping(value = "aKeySell")
    public MessageResult aKeySell(String coinUnit, BigDecimal money, BigDecimal amount, String[] pays,
                                  @SessionAttribute(SESSION_MEMBER) AuthMember user, HttpServletRequest request,
                                  @RequestParam(defaultValue = "0") Integer sellType, Long currencyId) throws UnexpectedException {
        // isTrue(checkOneKeyMoney(money, amount, coinUnit), msService.getMessage("PRICE_EXPIRED"));
        // isTrue("CNYT".equals(coinUnit), msService.getMessage("PRICE_EXPIRED"));
        log.info("币种:{},金额:{},出售类型:{},出售数量{},支付方式{}", coinUnit, money, sellType, amount, pays);
        Assert.notEmpty(pays, msService.getMessage("MISSING_PAY"));
        //验证用户默认法币配置跟限制是否一致 DCC取消验证
        if(!"DCC".equalsIgnoreCase(coinUnit)){
            MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
            isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
            isTrue(currencyId.equals(currencyManageMessageRespResult.getData().getId()), msService.getMessage("BASE_CURRENCY_NOT_SUPPORT_OTC_COIN"));
        }else{
            //默认CNY
            currencyId = 1L;
        }

        Member member = memberService.findOne(user.getId());
        memberService.checkRealName(member);
        // add by wsy, date: 2019-1-23 14:48:52，reason: 限制商家与商家交易， 只需要判断买家角色
        // isTrue(member.getMemberLevel() != MemberLevelEnum.IDENTIFICATION, msService.getMessage("SELLER_ALLOW_TRADE"));

        // add by wsy, date: 2019-1-23 14:48:52，reason: 限制买币用户必须实名认证
        // isTrue(member.getRealNameStatus() == RealNameStatus.VERIFIED, msService.getMessage("NO_REAL_NAME"));

        //获取币种信息
        OtcCoin otcCoin = otcCoinService.findByUnit(coinUnit);
        isTrue(otcCoin != null, msService.getMessage("COIN_ILLEGAL"));
        //add by tansitao 时间： 2018/5/14 原因：用户卖币增加限制
        if (1 == tradeConfig.getIsOpenLimit()) {
            isTrue(orderService.isAllowTrade(user.getId(), tradeConfig.getOrderNum()), msService.getMessage("NO_ALLOW_TRADE"));
            //限制卖出
            isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus() == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
            validateOpenTranscationService.validateOpenExPitTransaction(member.getId(), msService.getMessage("NO_ALLOW_TRANSACT_SELL"), AdvertiseType.SELL);
        }
        //add by  shenzucai 时间： 2019.04.29  原因：修复适配到浮动定价的广告没有正确记录价格的bug
        //获取法币价格
        BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(currencyId, coinUnit).getData();

        // OrderSourceType sourceType = OrderSourceType.convert(user.getPlatform());
        //获取支持一键卖币的商家
//        List<ClickBusinessConfig> clickBusinessConfigList = clickBusinessConfigService.getAllClickBusiness();
//         String appId = sourceType != null ? String.valueOf(sourceType.getOrdinal()) : user.getPlatform();
        //add by  shenzucai 时间： 2019.03.21  原因：使用同一的工具，处理appid
        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());

        List<ClickBusinessConfig> clickBusinessConfigList = clickBusinessConfigService.getAllClickBusiness(coinUnit, appId);
        List<Long> memberIds = new ArrayList<>();
        isTrue(clickBusinessConfigList != null && clickBusinessConfigList.size() > 0, msService.getMessage("NOT_EXIST_CLICK_BUSINESS"));
        //获取商家们的广告
        clickBusinessConfigList.forEach(clickBusiness -> memberIds.add(clickBusiness.getMemberId()));
        //edit by  shenzucai 时间： 2019.04.29  原因：传入浮动定价的广告
        List<Advertise> advertiseList;
        if (sellType == 1) {
            //如果是按照金额出售 按照金额筛选广告
            advertiseList = advertiseService.getByMoneyMemberIds(memberIds, money, AdvertiseType.BUY, otcCoin.getId(), currencyId);
        }else{
            advertiseList = advertiseService.getByMemberIds(memberIds, amount, marketPrice, AdvertiseType.BUY, otcCoin.getId(), currencyId);
        }
        isTrue(advertiseList != null && advertiseList.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        //声明可用的广告列表
        List<Advertise> availableAdvList = new ArrayList<>();
        //过滤出满足一键购币策略的广告
        for (Advertise tempAdvertise : advertiseList) {
            //如果是按照金额出售 按照金额计算出数量
            if (sellType == 1) {
                if(tempAdvertise.getPriceType() == PriceType.REGULAR){
                    amount = money.divide(tempAdvertise.getPrice(), 8, RoundingMode.DOWN);
                }else{
                    amount = money.divide(marketPrice,8,RoundingMode.DOWN);
                }
            }
            //判断剩余广告数量是否大于卖出币数
            if (tempAdvertise.getRemainAmount().compareTo(amount) >= 0) {
                //判断广告商是否为自己
                if (user.getId() != tempAdvertise.getBMemberId()) {
                    //判断广告支付方式是否包含用户选择的支付方式
                    for (String pay : pays) {
                        if (tempAdvertise.getPayMode().contains(pay)) {
                            availableAdvList.add(tempAdvertise);
                            break;
                        }
                    }
                }
            }
        }
        //如果没有可用广告抛出无广告提示
        isTrue(availableAdvList.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        // add by wsy data: 2019-1-24 14:10:13 reason: 增加商家订单数排序
        availableAdvList = filterAvailableAdvList(availableAdvList, AdvertiseType.BUY);

        //随机选择一个满足条件的广告
        Advertise advertise = availableAdvList.get(new Random().nextInt(availableAdvList.size()));
        if (sellType == 1) {
            if (advertise.getPriceType() == PriceType.MUTATIVE) {
                BigDecimal price = getRoundPrice(advertise, otcCoin, marketPrice);
                amount = money.divide(price, 8, RoundingMode.DOWN);
            } else {
                amount = money.divide(advertise.getPrice(), 8, RoundingMode.DOWN);
            }
        }
        Member bMember = memberService.findOne(advertise.getBMemberId());
        advertise.setMember(bMember);

        //add by zyj 2018.12.27 : 接入风控
        TradeCashInfo tradeCashInfo = new TradeCashInfo();
        tradeCashInfo.setDirection(1);
        tradeCashInfo.setCoin(otcCoin.getUnit());
        tradeCashInfo.setAmount(amount);
        tradeCashInfo.setTargetUser(memberService.findOne(advertise.getBMemberId()));

        MessageResult res = risk(request, null, member, tradeCashInfo);
        if (res.getCode() != 0) {
            return error(res.getMessage());
        }

        //计算手续费
        BigDecimal commission;
        if ("USDC".equals(otcCoin.getUnit())) {
            commission = getCommission(amount, 1);
        } else {
            commission = mulRound(amount, getRate(otcCoin.getBuyJyRate()));
            //手续费折扣率
            BigDecimal feeDiscount = BigDecimal.ZERO;
            if (otcCoin.getFeeBuyDiscount().compareTo(BigDecimal.ONE) >= 0) {
                feeDiscount = commission;
            } else {
                feeDiscount = commission.multiply(otcCoin.getFeeBuyDiscount());
                BigDecimal remainingFee = commission.subtract(feeDiscount); //优惠后的当前手续费
                //计算 当前会员可优惠手续费数量
                BigDecimal memberFeeDiscount = remainingFee.multiply(
                        businessDiscountRuleService.getDiscountRule(bMember.getId(),
                                otcCoin.getUnit()).getBuyDiscount());

                feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
            }
            commission = commission.subtract(feeDiscount);
        }

        MemberWallet wallet = memberWalletService.findByOtcCoinAndMemberId(otcCoin, user.getId());
        isTrue(compare(wallet.getBalance(), amount), msService.getMessage("INSUFFICIENT_BALANCE"));

        //add by tansitao 时间： 2018/11/9 原因：增加卖家和买家的账户姓名处理，有账户的用账户姓名，没有的用真实姓名
        MemberPaymentAccount cmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
        MemberPaymentAccount bmemberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(bMember.getId());
        String caccountName = cmemberPaymentAccount == null || StringUtils.isEmpty(cmemberPaymentAccount.getAccountName())
                ? member.getRealName() : cmemberPaymentAccount.getAccountName();
        String baccountName = bmemberPaymentAccount == null || StringUtils.isEmpty(bmemberPaymentAccount.getAccountName())
                ? bMember.getRealName() : bmemberPaymentAccount.getAccountName();

        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(1000, 9999));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        //add by ss 时间：2020/03/24 原因：新增法币
        order.setCurrencyId(advertise.getCurrencyId());
        order.setCountry(advertise.getCountryName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(caccountName);
        order.setMemberId(bMember.getId());
        order.setMemberName(bMember.getUsername());
        order.setMemberRealName(baccountName);
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        if (advertise.getPriceType() == PriceType.MUTATIVE) {
            BigDecimal price = getRoundPrice(advertise, otcCoin, marketPrice);
            order.setPrice(price);
            order.setMoney(price.multiply(amount));
        } else {
            order.setPrice(advertise.getPrice());
            order.setMoney(amount.multiply(advertise.getPrice()));
        }
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setMoney(order.getMoney().setScale(2, RoundingMode.UP));
        // add by wsy, 时间：2019-3-21 09:34:56 原因：收取用户服务费给认证商家
        order.setOrderMoney(order.getMoney());
        if (bMember.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED) {
            // 计算用户服务费
            this.serviceMoney(order, order.getMoney());
        }
        if ("USDC".equals(otcCoin.getUnit())) {
            //USDC 需要减去手续费
            order.setMoney(order.getMoney().subtract(order.getCommission().multiply(order.getPrice()).setScale(2, RoundingMode.UP)));
        }
        /*//判断价格类型
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            //固定价格直接设置
            order.setPrice(advertise.getPrice());
        } else {
            //溢价则要计算溢价多少
            //del by  shenzucai 时间： 2019.04.29  原因：将marketPrice放到前面获取，用于价格运算
            // BigDecimal marketPrice = coins.get(otcCoin.getUnit());
            BigDecimal price = mulRound(rate(advertise.getPremiseRate()), marketPrice, otcCoin.getCoinScale());
            order.setPrice(price);
        }*/
        order.setTimeLimit(advertise.getTimeLimit());
        order.setPayCode(randomCode);
        order.setOrderSourceType(AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform()));
        order.setIsOneKey(BooleanEnum.IS_TRUE);

        String[] pay = advertise.getPayMode().split(",");
        MessageResult result = MessageResult.error(msService.getMessage("CREATE_ORDER_SUCCESS"));
        //del by ss 时间：2020/03/24 原因：支付方式不再保存到订单
//        Arrays.stream(pay).forEach(x -> {
//            if (ALI.getCnName().equals(x)) {
//                if (member.getAlipay() != null) {
//                    result.setCode(0);
//                    order.setAlipay(member.getAlipay());
//                }
//            } else if (WECHAT.getCnName().equals(x)) {
//                if (member.getWechatPay() != null) {
//                    result.setCode(0);
//                    order.setWechatPay(member.getWechatPay());
//                }
//            } else if (BANK.getCnName().equals(x)) {
//                if (member.getBankInfo() != null) {
//                    result.setCode(0);
//                    order.setBankInfo(member.getBankInfo());
//                }
//            } else if (EPAY.getCnName().equals(x)) {
//                MemberPaymentAccount memberPaymentAccount = memberPaymentAccountService.findPaymentAccountByMemberId(member.getId());
//                if (memberPaymentAccount != null) {
//                    result.setCode(0);
//                    Epay epay = new Epay();
//                    epay.setEpayNo(memberPaymentAccount.getEpayNo());
//                    order.setEpay(epay);
//                }
//            }
//        });

        //edit by tansitao 时间： 2018/11/10 原因：取消对广告支付方式的控制
        result.setCode(0);

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        Order order1 = orderService.sellOrder(order, advertise, wallet, amount);
        if (order1 != null) {
            if (advertise.getAuto() == BooleanEnum.IS_TRUE && !StringUtils.isEmpty(advertise.getAutoword())) {
                //edit by yangch 时间： 2018.07.12 原因：修改为异步推送自动回复内容
                pushOrderMessageService.pushAutoResponseMessage2Mongodb(advertise, order1);
            }

            //edit by yangch 时间： 2018.07.12 原因：修改为异步发送订单短信 20190929
//            pushOrderMessageService.pushCreateOrderMessage4SMS(advertise, order1, user);
            //add by tansitao 时间： 2018/11/19 原因：下单成功，redis中的订单数加一
            redisCountorService.addOrSubtractHash(SysConstant.C2C_MONITOR_ORDER + advertise.getBMemberId() + "-" + advertise.getId(), SysConstant.C2C_ONLINE_NUM, 1L);

            getService().creatOrderEvent(order1, user);
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCMB003：%s", msService.getMessage("SELL_FAILED")));
        }
    }

    /**
     * 接入风控 modify by qhliao 因商家购币通道需要调用故改为public
     *
     * @param request
     * @param device
     * @param member
     * @param tradeCashInfo
     * @author Zhang Yanjun
     * @time 2018.12.27 14:27
     */
    public MessageResult risk(HttpServletRequest request, DeviceInfo device, Member member, TradeCashInfo tradeCashInfo) {
        return iTradeCashEvent.tradeCash(request, device, member, tradeCashInfo);
    }

    /**
     * 计算服务费
     */
    private void serviceMoney(Order order, BigDecimal money) {
        // 计算用户服务费
        try {
            BigDecimal rate = ConvertUtils.lookup(BigDecimal.class).convert(BigDecimal.class, otcCoinService.getServiceRate(order.getCoin().getUnit()));
            BigDecimal rateMoney = money.multiply(rate).setScale(2, BigDecimal.ROUND_DOWN);
            order.setServiceMoney(rateMoney);
            order.setMoney(money.subtract(order.getServiceMoney()));
            order.setOrderMoney(money);
            order.setServiceRate(rate);
        } catch (Exception e) {
            log.error("配置错误，无法扣除用户服务费", e);
        }
    }

    /**
     * 获取当前会员所有OTC订单
     *
     * @param user     会员信息
     * @param status   状态（0：已取消；1：未付款；2：已付款；3：已完成；4：申诉中；5：已关闭）
     * @param pageNo   页数
     * @param pageSize 条数
     * @param money    金额（￥）
     * @return 当前会员所有OTC订单
     * @author zhongxiaoj
     * @date 2019.08.02
     * @desc 在原有基础上，增加根据金额精确匹配
     */
    @RequestMapping(value = "self")
    public MessageResult myOrder(@SessionAttribute(SESSION_MEMBER) AuthMember user, OrderStatus status, int pageNo, int pageSize, String orderSn, String unit, AdvertiseType type, BigDecimal money) {
        Page<Order> page = orderService.pageQuery(pageNo, pageSize, status, user.getId(), orderSn, unit, type, money);
        List<Long> memberIdList = new ArrayList<>();
        page.forEach(order -> {
            if (!memberIdList.contains(order.getMemberId())) {
                memberIdList.add(order.getMemberId());
            }
            if (!memberIdList.contains(order.getCustomerId())) {
                memberIdList.add(order.getCustomerId());
            }
        });
        Page<ScanOrder> scanOrders = page.map(x -> {
            ScanOrder scanOrder = ScanOrder.toScanOrder(x, user.getId());
            if("USDC".equals(x.getCoin().getUnit())){
                if(x.getAdvertiseType() == AdvertiseType.BUY){
                    scanOrder.setMoney(x.getNumber().subtract(x.getCommission()).multiply(x.getPrice()).setScale(2,BigDecimal.ROUND_DOWN));
                }
                if(scanOrder.getType() == AdvertiseType.BUY && scanOrder.getAdvertiseType() == AdvertiseType.BUY){
                    //商家订单列表数量展示为number减去手续费
                    scanOrder.setAmount(scanOrder.getAmount().subtract(x.getCommission()));
                }else if(scanOrder.getType() == AdvertiseType.SELL && scanOrder.getAdvertiseType() == AdvertiseType.SELL){
                    //商家订单列表数量展示为number减去手续费
                    scanOrder.setMoney(x.getOrderMoney());
                }
                scanOrder.setCommission((user.getId() == x.getMemberId() ? BigDecimal.ZERO : x.getCommission()));
            }
            return scanOrder;

        });
        List<CurrencyManage> currencyManageList = scanOrders.getContent().size() > 0 ? iOtcServerV2Service.getAllCurrency().getData() : null;
        for (ScanOrder scanOrder : scanOrders) {
            for (CurrencyManage c : currencyManageList) {
                if (c.getId().equals(scanOrder.getCurrencyId())) {
                    scanOrder.setCurrencyName(c.getName());
                    scanOrder.setCurrencySymbol(c.getSymbol());
                    scanOrder.setCurrencyUnit(c.getUnit());
                    break;
                }
            }
            scanOrder.setCountry(countryService.findOne(scanOrder.getCountryName()));
        }
        MessageResult result = MessageResult.success();
        result.setData(scanOrders);
        return result;
    }

    /**
     * 历史订单查询
     *
     * @param status
     * @param pageNo
     * @param pageSize
     * @param orderSn
     * @param unit
     * @param type     交易类型 0买 1卖
     * @param money    金额，精确匹配
     * @return
     */
    @RequestMapping(value = "orderHistory")
    public MessageResult selfNew(@SessionAttribute(SESSION_MEMBER) AuthMember user,
                                 OrderStatus status, int pageNo, int pageSize, String orderSn, String unit, AdvertiseType type, BigDecimal money) {
        int t;
        int s;
        if (type == null) {
            t = -1;
        } else {
            t = type.getOrdinal();
        }
        if (status == null) {
            s = -1;
        } else {
            s = status.getOrdinal();
        }
        PageInfo<MyOrderVO> pageInfo = otcOrderService.findOrderBy(pageNo, pageSize, user.getId(), s, orderSn, unit, t, money);
        List<CurrencyManage> currencyManageList = pageInfo.getList().size() > 0 ? iOtcServerV2Service.getAllCurrency().getData() : null;
        pageInfo.getList().stream().forEach(order -> {
            for (CurrencyManage c : currencyManageList) {
                if (c.getId().equals(order.getCurrencyId())) {
                    order.setCurrencyName(c.getName());
                    order.setCurrencySymbol(c.getSymbol());
                    order.setCurrencyUnit(c.getUnit());
                    break;
                }
            }
        });
        return success("查询成功", PageData.toPageData(pageInfo));
    }

    /**
     *  * 获取某个用户的所有进行中订单
     *  * @author tansitao
     *  * @time 2018/12/26 16:52 
     *  
     */
    @RequestMapping(value = "allGoingOrder")
    public MessageResult allGoingOrder(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<Order> orderList = otcOrderService.getAllGoingOrder(user.getId());
        List<ScanOrder> scanOrders = new ArrayList<>();
        //获取法币列表
        List<CurrencyManage> currencyManageList = iOtcServerV2Service.getAllCurrency().getData();
        orderList.forEach(order -> {
            ScanOrder s = ScanOrder.toScanOrder(order, user.getId());
            s.setCurrencyId(order.getCurrencyId());
            for (CurrencyManage c : currencyManageList) {
                if (c.getId().equals(order.getCustomerId())) {
                    s.setCurrencyName(c.getName());
                    s.setCurrencySymbol(c.getSymbol());
                    s.setCurrencyUnit(c.getUnit());
                    break;
                }
            }
            scanOrders.add(s);
        });
        for (ScanOrder scanOrder : scanOrders) {
            scanOrder.setCountry(countryService.findOne(scanOrder.getCountryName()));
        }
        MessageResult result = MessageResult.success();
        result.setData(scanOrders);
        return result;
    }

    /**
     * 订单详情
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "detail")
    public MessageResult queryOrder(String orderSn, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = MessageResult.success();
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        Member businessMember = memberService.findOne(order.getMemberId());//add by tansitao 时间： 2018/9/4 原因：获取商家信息
        Member customerMember = memberService.findOne(order.getCustomerId());//add by tansitao 时间： 2018/9/4 原因：获取消费者信息
        Advertise advertise = advertiseService.findById(order.getAdvertiseId());
        CurrencyManage currencyManage = iOtcServerV2Service.getCurrencyById(advertise.getCurrencyId()).getData();
//        Appeal appeal = appealService.findByOrderId(String.valueOf(order.getId()));//add by tansitao 时间： 2018/9/4 原因：订单申诉信息
        //edit by zyj 2018-12-17 修改为 查询最新的申诉信息
        Appeal appeal = appealService.findNewByorderId(String.valueOf(order.getId()));
        List<OrderAppealAccessory> orderAppealAccessories = null;
        List<AdminOrderAppealSuccessAccessory> orderAppealSuccessAccessories = null;
        //add by tansitao 时间： 2018/11/8 原因：对申诉材料进行处理
        if (appeal != null) {
            orderAppealAccessories = appealService.findAppealImgById(appeal.getId());
            if (orderAppealAccessories != null && orderAppealAccessories.size() > 0) {
                for (OrderAppealAccessory orderAppealAccessory : orderAppealAccessories) {
                    try {
                        //add by tansitao 时间： 2018/11/8 原因：增加对地址的处理
                        String uri = AliyunUtil.getPrivateUrl(aliyunConfig, orderAppealAccessory.getUrlPath());
                        orderAppealAccessory.setUrlPath(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.setCode(500);
                        result.setMessage(msService.getMessage("OSS_FAIL"));//edit by tansitao 时间： 2018/5/11 原因：修改国际化认证
                    }
                }
            }
        }

        //add by tansitao 时间： 2018/11/8 原因：对申诉材完成后材料进行处理
        if (appeal != null && appeal.getStatus() == AppealStatus.PROCESSED) {
            orderAppealSuccessAccessories = adminOrderAppealSuccessAccessoryService.findByAppealId(appeal.getId());
            if (orderAppealSuccessAccessories != null && orderAppealSuccessAccessories.size() > 0) {
                for (AdminOrderAppealSuccessAccessory orderAppealSuccessAccessorie : orderAppealSuccessAccessories) {
                    try {
                        //add by tansitao 时间： 2018/11/8 原因：增加对地址的处理
                        String uri = AliyunUtil.getPrivateUrl(aliyunConfig, orderAppealSuccessAccessorie.getUrlPath());
                        orderAppealSuccessAccessorie.setUrlPath(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.setCode(500);
                        result.setMessage(msService.getMessage("OSS_FAIL"));//edit by tansitao 时间： 2018/5/11 原因：修改国际化认证
                    }
                }
            }
        }
        //获取订单最终的支付方式
        OrderDetail info = OrderDetail.builder().orderSn(orderSn)
                .unit(order.getCoin().getUnit())
                .status(order.getStatus())
                .amount(order.getNumber())
                .price(order.getPrice())
                .money(order.getMoney())
                .payTime(order.getPayTime())
                .createTime(order.getCreateTime())
                .timeLimit(order.getTimeLimit())
                .myId(user.getId())
                //add by ss 时间2020/03/28 原因：法币信息
                .currencyId(currencyManage.getId())
                .currencyName(currencyManage.getName())
                .currencySymbol(currencyManage.getSymbol())
                .currencyUnit(currencyManage.getUnit())
                .payCode(order.getPayCode())
                .remark(order.getRemark()) //add by yangch 时间： 2018.05.05 原因：顾客需求
                .country(countryService.findOne(order.getCountry())) //add by tansitao 时间： 2018/8/15 原因：增加国家
                .isManualCancel(order.getIsManualCancel())//add by tansitao 时间： 2018/8/15 原因：增加是否为手动取消
                .traderNum(user.getId() == businessMember.getId() ? customerMember.getTransactions() : businessMember.getTransactions())//add by tansitao 时间： 2018/8/15 原因：增加商家交易数量
//                .payMode(order.getPayMethod())//add by tansitao 时间： 2018/8/15 原因：增加支付方式
                .payKey(getPayKeyById(order.getPayMethod() != null ? order.getPayMethod() + 1 : null))
                .payModeInfo(order.getPayMethodInfo() == null ? null : JSONObject.parseObject(order.getPayMethodInfo())) // add by wsy 时间：2019-5-27 18:58:46 原因：增加支付方式账号信息
                .appealStatus(appeal == null ? null : appeal.getStatus())//add by tansitao 时间： 2018/9/4 原因：订单申诉状态
                .businessNickname(businessMember.getUsername())//add by tansitao 时间： 2018/9/5 原因：商家昵称
                .appeal(appeal) //add by tansitao 时间： 2018/11/2 原因：增加申诉记录
                .orderAppealAccessories(orderAppealAccessories) //add by tansitao 时间： 2018/11/2 原因：申诉的材料
                .orderAppealSuccessAccessories(orderAppealSuccessAccessories) //add by tansitao 时间： 2018/11/2 原因：申诉处理后的材料
                //edit by tansitao 时间： 2018/12/11 原因：修复修改广告后支付方式改变的问题
                .supportPayModes(order.getPayMode())
//                .isSupportAliPay(order.getPayMode().indexOf(ALI.getCnName()) != -1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE)//add by tansitao 时间： 2018/11/10 原因：设置是否支持该支付方式
//                .isSupportWechatPay(order.getPayMode().indexOf(WECHAT.getCnName()) != -1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE)//add by tansitao 时间： 2018/11/10 原因：设置是否支持该支付方式
//                .isSupportBank(order.getPayMode().indexOf(BANK.getCnName()) != -1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE)//add by tansitao 时间： 2018/11/10 原因：设置是否支持该支付方式
//                .isSupportEPay(order.getPayMode().indexOf(EPAY.getCnName()) != -1 ? BooleanEnum.IS_TRUE : BooleanEnum.IS_FALSE)//add by tansitao 时间： 2018/11/10 原因：设置是否支持该支付方式
                .orderMoney(order.getOrderMoney()) // add by wsy: 时间：2019-3-21 09:27:26 原因：用户买币收取一定比例服务费
                .serviceMoney(order.getServiceMoney()) // add by wsy: 时间：2019-3-21 09:27:26 原因：用户买币收取一定比例服务费
                .serviceRate(order.getServiceRate()) // add by wsy: 时间：2019-3-21 09:27:26 原因：用户买币收取一定比例服务费
                .businessVerified(businessMember.getCertifiedBusinessStatus() == CertifiedBusinessStatus.VERIFIED ? IS_TRUE : IS_FALSE) // add by wsy, data:2019-3-21 09:36:34 是否为认证商家
                .advertiseType(advertise.getAdvertiseType()) // add by wsy, data:2019-3-21 09:36:34 广告类型：买入或者卖出
                .build();
//
//        try {
//            if (order.getAlipay() != null) {
//                // 生成支付宝认证后的图片url
//                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, order.getAlipay().getQrCodeUrl());
//                order.getAlipay().setQrCodeUrl(uri);
//            }
//            if (order.getWechatPay() != null) {
//                // 生成微信认证后的图片url
//                String uri = AliyunUtil.getPrivateUrl(aliyunConfig, order.getWechatPay().getQrWeCodeUrl());
//                order.getWechatPay().setQrWeCodeUrl(uri);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.setCode(500);
//            result.setMessage(msService.getMessage("OSS_FAIL"));//edit by tansitao 时间： 2018/5/11 原因：修改国际化认证
//        }

//        PayInfo payInfo = PayInfo.builder()
//                .bankInfo(order.getBankInfo())
//                .alipay(order.getAlipay())
//                .wechatPay(order.getWechatPay())
//                .epay(order.getEpay())
//                .build();

        if(StringUtils.hasText(order.getPayMethodInfo()) && JSONObject.parseObject(order.getPayMethodInfo()).get("payInfo") != null){
            //处理旧的订单支付方式
            Map<String,String> payInfo = Maps.newHashMap();
            payInfo.put("realName",JSONObject.parseObject(order.getPayMethodInfo()).getString("realName"));
            JSONObject jsonObject = (JSONObject) JSONObject.parseObject(order.getPayMethodInfo()).get("payInfo");
            if(jsonObject.getString("aliNo") != null){
                //支付宝
                payInfo.put("accountNo",jsonObject.getString("aliNo"));
                if(jsonObject.getString("qrCodeUrl") != null){
                    payInfo.put("qrCode",AliyunUtil.getPrivateUrl(aliyunConfig, jsonObject.getString("qrCodeUrl")));
                }
            }else if(jsonObject.getString("bank") != null){
                //银行卡
                payInfo.put("bank",jsonObject.getString("bank"));
                if(jsonObject.getString("branch") != null){
                    payInfo.put("branch",jsonObject.getString("branch"));
                }
                if(jsonObject.getString("cardNo") != null){
                    payInfo.put("accountNo",jsonObject.getString("cardNo"));
                }

            }else if(jsonObject.getString("wechat") != null){
                //微信
                payInfo.put("accountNo",jsonObject.getString("wechat"));
                if(jsonObject.getString("wechatNick") != null){
                    payInfo.put("nick",jsonObject.getString("wechatNick"));
                }
                if(jsonObject.getString("qrWeCodeUrl") != null){
                    payInfo.put("qrCode",AliyunUtil.getPrivateUrl(aliyunConfig, jsonObject.getString("qrWeCodeUrl")));
                }

            }else if(jsonObject.getString("epayNo") != null){
                //微信
                payInfo.put("accountNo",jsonObject.getString("epayNo"));
            }
            info.setPayModeInfo((JSONObject)JSONObject.toJSON(payInfo));
        }

        if (order.getMemberId().equals(user.getId())) {
            //是商家
            info.setIsBusiness(IS_TRUE);
            info.setHisId(order.getCustomerId());
            info.setOtherSide(order.getCustomerName());
            info.setOtherPhone(customerMember.getMobilePhone());//add by tansitao 时间： 2018/9/4 原因：增加对方手机号
            info.setOtherEmail(customerMember.getEmail());//add by tansitao 时间： 2018/11/13 原因：对方邮箱
            info.setCommission(order.getCommission());
            if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
                info.setType(AdvertiseType.BUY);
                info.setPayInfo(iOtcServerV2Service.queryCapSettingByMember(order.getCustomerId()).getData());
                //add by ss 时间：2020/03/31 原因：USDC区别对待
                if ("USDC".equalsIgnoreCase(info.getUnit())) {
                    info.setCommission(order.getCommission());
                    info.setMoney(info.getAmount().subtract(info.getCommission()).multiply(info.getPrice()).setScale(2,BigDecimal.ROUND_DOWN));
                    info.setServiceMoney(order.getCommission());
                    info.setServiceRate(getUSDCService(1));
                }
//                if (info.getPayInfo() != null) {
//                    info.getPayInfo().setRealName(order.getCustomerRealName());
//                }
            } else {
                //add by ss 时间：2020/03/31 原因：USDC区别对待
                if ("USDC".equalsIgnoreCase(info.getUnit())) {
                    info.setCommission(order.getCommission());
                    info.setServiceMoney(order.getCommission());
                    info.setServiceRate(getUSDCService(0));
                }
                info.setPayInfo(iOtcServerV2Service.queryCapSettingByMember(order.getMemberId()).getData());
                info.setType(AdvertiseType.SELL);
//                if (info.getPayInfo() != null) {
//                    info.getPayInfo().setRealName(order.getMemberRealName());
//                }
            }
        } else if (order.getCustomerId().equals(user.getId())) {
            //是用户
            info.setIsBusiness(IS_FALSE);
            info.setHisId(order.getMemberId());
            info.setOtherSide(order.getMemberName());
            info.setOtherPhone(businessMember.getMobilePhone());//add by tansitao 时间： 2018/9/4 原因：增加对方手机号
            info.setOtherEmail(businessMember.getEmail());//add by tansitao 时间： 2018/11/13 原因：对方邮箱
            info.setCommission(BigDecimal.ZERO);

            if (order.getAdvertiseType().equals(AdvertiseType.BUY)) {
                info.setPayInfo(iOtcServerV2Service.queryCapSettingByMember(order.getCustomerId()).getData());
//                if (info.getPayInfo() != null) {
//                    info.getPayInfo().setRealName(order.getCustomerRealName());
//                }
                info.setType(AdvertiseType.SELL);
                //add by ss 时间：2020/03/31 原因：USDC区别对待
                if ("USDC".equalsIgnoreCase(info.getUnit())) {
                    info.setCommission(order.getCommission());
                    info.setMoney(info.getAmount().subtract(info.getCommission()).multiply(info.getPrice()).setScale(2,BigDecimal.ROUND_DOWN));
                    info.setServiceMoney(order.getCommission());
                    info.setServiceRate(getUSDCService(1));
                }
            } else {
                //add by ss 时间：2020/03/31 原因：USDC区别对待
                if ("USDC".equalsIgnoreCase(info.getUnit())) {
                    info.setCommission(order.getCommission());
                    info.setServiceMoney(order.getCommission());
                    info.setServiceRate(getUSDCService(0));
                }
                info.setPayInfo(iOtcServerV2Service.queryCapSettingByMember(order.getMemberId()).getData());
//                if (info.getPayInfo() != null) {
//                    info.getPayInfo().setRealName(order.getMemberRealName());
//                }
                info.setType(AdvertiseType.BUY);
            }
        } else {
            return MessageResult.error(msService.getMessage("ORDER_NOT_EXISTS"));
        }
        result.setData(info);
        return result;
    }

    /**
     * 根据交易方式ID获取交易方式payKey
     *
     * @param payMethod
     * @return
     */
    private String getPayKeyById(Long payMethod) {
        if (payMethod == null) {
            return null;
        }
        List<PaySetting> paySettingList = iOtcServerV2Service.getPaySettings(-1L).getData();
        for (PaySetting paySetting : paySettingList) {
            if (payMethod.equals(paySetting.getId())) {
                return paySetting.getPayKey();
            }
        }
        return null;
    }

    /**
     * 获取USDC手续费率
     *
     * @param type 1：USDC用户出售手续费 0：USDC用户购买手续费
     * @return
     */
    private BigDecimal getUSDCService(int type) {

        BigDecimal serviceRate;
        switch (type) {
            case 1:
                //购买广告，对应用户出售 USDC用户出售手续费 USDC_SELL_CHARGE
                serviceRate = new BigDecimal(iOtcServerV2Service.getCurrencyRuleValueByKey("USDC_SELL_CHARGE").getData());
                break;
            case 0:
                //出售广告，对应用户购买 USDC用户购买手续费 USDC_PAY_CHARGE
                serviceRate = new BigDecimal(iOtcServerV2Service.getCurrencyRuleValueByKey("USDC_PAY_CHARGE").getData());
                break;
            default:
                serviceRate = BigDecimal.ZERO;
        }
        return serviceRate;
    }

    /**
     * 取消订单
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "cancel")
    public MessageResult cancelOrder(String orderSn, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws UnexpectedException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));

        //edit by yangch 时间： 2018.04.28 原因：解决买家已付款，卖家可取消订单的情况（防止通信异常，取消按钮可以操作的情况）
        if (order.getStatus() == OrderStatus.PAID) {
            throw new UnexpectedException(msService.getMessage("CAN_NOT_CANCEL"));
        }
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));

        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该会员是广告发布者，购买类型的广告，并且是付款者
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            //代表该会员不是广告发布者，并且是付款者
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

        //add by tansitao 时间： 2019/1/4 原因：封装取消订单方法
        orderService.cancelOrderByhandle(order, user.getId(), ret);

        //add by tansitao 时间： 2018/11/19 原因：取消订单成功，redis中的订单数减一（前提是单数不为空并且大于0）
        redisCountorService.subtractHash(SysConstant.C2C_MONITOR_ORDER + order.getMemberId() + "-" + order.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
        // 事件埋点
        getService().cancelOrderEvent(user, orderSn);
        return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
    }

    /**
     * 取消BtBank-otc-api订单
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "cancelBOA")
    public MessageResult cancelBOAOrder(String orderSn, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws UnexpectedException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));

        //edit by yangch 时间： 2018.04.28 原因：解决买家已付款，卖家可取消订单的情况（防止通信异常，取消按钮可以操作的情况）
        if (order.getStatus() == OrderStatus.PAID) {
            throw new UnexpectedException(msService.getMessage("CAN_NOT_CANCEL"));
        }
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));

        // 该方法只能是btbank-otc-api 的商家调用

        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该会员是广告发布者，购买类型的广告，并且是付款者
            ret = 1;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        //add by tansitao 时间： 2019/1/4 原因：封装取消订单方法
        orderService.cancelOrderByhandlecancelBOA(order, user.getId(), ret);
        getService().cancelOrderEvent(user, orderSn);
        return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
    }


    /**
     * 确认付款 BTBANK-OTC-API订单
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "payBOA")
    public MessageResult payOrderBOA(String orderSn, @SessionAttribute(SESSION_MEMBER) AuthMember user, PayMode payMode, String realName,
                                     Alipay alipay, WechatPay wechatPay, BankInfo bankInfo, Epay epay) throws UnexpectedException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT), msService.getMessage("ORDER_STATUS_EXPIRED"));
        isTrue(compare(new BigDecimal(order.getTimeLimit()), DateUtil.diffMinute(order.getCreateTime())), msService.getMessage("ORDER_ALREADY_AUTO_CANCEL"));

        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该会员是广告发布者，并且是付款者
            ret = 1;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

        // add by wsy, 时间：2019-5-29 16:57:39 原因：付款方可以修改支付信息，并填写到数据库
        String payModeInfo = handlePayModeInfo(user, payMode, realName, alipay, wechatPay, bankInfo, epay);
        int is = orderService.payForOrderBOA(orderSn, payMode, payModeInfo);
        if (is > 0) {
            /**
             * 聚合otc订单手续费等明细存入mongodb
             */
            OrderDetailAggregation aggregation = new OrderDetailAggregation();
            BeanUtils.copyProperties(order, aggregation);
            aggregation.setUnit(order.getCoin().getUnit());
            aggregation.setOrderId(order.getOrderSn());
            aggregation.setFee(order.getCommission().doubleValue());
            aggregation.setAmount(order.getNumber().doubleValue());
            aggregation.setType(OrderTypeEnum.OTC);
            aggregation.setTime(Calendar.getInstance().getTimeInMillis());
            orderDetailAggregationService.save(aggregation);

            //edit by yangch 时间： 2018.07.12 原因：修改为异步推送支付成功的订单短信
            pushOrderMessageService.pushPayOrderMessage4SMS(order, user);
            MessageResult result = MessageResult.success(msService.getMessage("PAY_SUCCESS"));
            order.setPayMethodInfo(payModeInfo);
            result.setData(order);
            // 事件埋点
            getService().payOrder(user, orderSn);
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCFK001：%s", msService.getMessage("PAY_FAILED")));
        }
    }

    /**
     * 主动取消订单
     *
     * @param user
     * @param orderSn
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_CANCEL_ORDER, memberId = "#user.getId()", refId = "#orderSn")
    public void cancelOrderEvent(AuthMember user, String orderSn) {
    }

    /**
     * 确认付款
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "pay")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payModeId", value = "支付方式ID", required = false, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "payKey", value = "交易方式key", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "payModeInfo", value = "支付方式内容(JSON字符串)", required = false, dataType = "Long", paramType = "query"),
    })
    public MessageResult payOrder(String orderSn, @SessionAttribute(SESSION_MEMBER) AuthMember user, Long payModeId, String payKey,
                                  String payModeInfo) throws UnexpectedException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT), msService.getMessage("ORDER_STATUS_EXPIRED"));
        isTrue(compare(new BigDecimal(order.getTimeLimit()), DateUtil.diffMinute(order.getCreateTime())), msService.getMessage("ORDER_ALREADY_AUTO_CANCEL"));
        List<PaySetting> paySettingList = iOtcServerV2Service.getPaySettings(-1L).getData();
        //存交易方式快照
        order.setAttr1("1");
        if(paySettingList != null && paySettingList.size() > 0){
            order.setAttr1(JSONObject.toJSONString(paySettingList));
        }
        //交易方式，由于特殊原因，pc端传payModeId为交易方式ID-1，app端传payKey，需要通过payKey获取交易方式ID
        if (payModeId == null) {
            //没有payModeId做APP端处理
            notNull(payKey, msService.getMessage("MISSING_PAY_MODE"));
            for (PaySetting p : paySettingList) {
                if (p.getPayKey().equals(payKey)) {
                    payModeId = p.getId() - 1;
                }
            }
        }

        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该会员是广告发布者，并且是付款者
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            //代表该会员不是广告发布者
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

        // add by wsy, 时间：2019-5-29 16:57:39 原因：付款方可以修改支付信息，并填写到数据库
//        payModeInfo = handlePayModeInfo(user, payMode, realName, alipay, wechatPay, bankInfo, epay);
//        int is = orderService.payForOrder(orderSn, payMode, payModeInfo);
        int is = orderService.orderPayMethod(orderSn, payModeId, payModeInfo,order.getAttr1());
        if (is > 0) {
            /**
             * 聚合otc订单手续费等明细存入mongodb
             */
            OrderDetailAggregation aggregation = new OrderDetailAggregation();
            BeanUtils.copyProperties(order, aggregation);
            aggregation.setUnit(order.getCoin().getUnit());
            aggregation.setOrderId(order.getOrderSn());
            aggregation.setFee(order.getCommission().doubleValue());
            aggregation.setAmount(order.getNumber().doubleValue());
            aggregation.setType(OrderTypeEnum.OTC);
            aggregation.setTime(Calendar.getInstance().getTimeInMillis());
            orderDetailAggregationService.save(aggregation);

            //edit by yangch 时间： 2018.07.12 原因：修改为异步推送支付成功的订单短信 20190929
//            pushOrderMessageService.pushPayOrderMessage4SMS(order, user);
            MessageResult result = MessageResult.success(msService.getMessage("PAY_SUCCESS"));
            order.setPayMethodInfo(payModeInfo);
            result.setData(order);
            // 事件埋点
            getService().payOrder(user, orderSn);
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCFK001：%s", msService.getMessage("PAY_FAILED")));
        }
    }

    private String handlePayModeInfo(AuthMember user, PayMode payMode, String realName, Alipay alipay, WechatPay wechatPay, BankInfo bankInfo, Epay epay) {
        Map<String, Object> map = new HashMap<>();
        long userId = user.getId();
        try {
            switch (payMode) {
                case ALI:
                    if (StringUtils.isEmpty(alipay.getAliNo())) {
                        map.put("payInfo", memberService.findOne(userId).getAlipay());
                    } else {
                        map.put("payInfo", alipay);
                    }

                    break;
                case WECHAT:
                    if (StringUtils.isEmpty(wechatPay.getWechat())) {
                        map.put("payInfo", memberService.findOne(userId).getWechatPay());
                    } else {
                        map.put("payInfo", wechatPay);
                    }
                    break;
                case BANK:
                    if (StringUtils.isEmpty(bankInfo.getBank()) || StringUtils.isEmpty(bankInfo.getCardNo())) {
                        map.put("payInfo", memberService.findOne(userId).getBankInfo());
                    } else {
                        map.put("payInfo", bankInfo);
                    }
                    break;
                case EPAY:
                    if (StringUtils.isEmpty(epay.getEpayNo())) {
                        Epay epayInfo = new Epay();
                        epayInfo.setEpayNo(memberPaymentAccountService.findPaymentAccountByMemberId(userId).getEpayNo());
                        map.put("payInfo", epayInfo);
                    } else {
                        map.put("payInfo", epay);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("标记支付方式时，付款方信息处理异常：", e);
        } finally {
            map.put("realName", StringUtils.isEmpty(realName) ? user.getRealName() : realName);
        }
        return JSON.toJSONString(map);
    }

    /**
     * 订单放行
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "release")
    public MessageResult confirmRelease(String orderSn, String jyPassword, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_STATUS_EXPIRED"));

        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getCustomerId().equals(user.getId())) {
            //代表该会员不是广告发布者，并且是放行者：用户
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getMemberId().equals(user.getId())) {
            //代表该会员是广告发布者，并且是放行者：商家
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        orderService.payOrder(order, ret, user);

        //edit by yangch 时间： 2018.07.12 原因：异步推送放行订单的短信提示 20190929
//        pushOrderMessageService.pushReleasedOrderMessage4SMS(order, user);
        orderEvent.onOrderCompleted(order); //订单放币完成的后续处理
        //add by tansitao 时间： 2018/11/19 原因：订单完成，redis中的订单数减一（前提是单数不为空并且大于0）
        redisCountorService.subtractHash(SysConstant.C2C_MONITOR_ORDER + order.getMemberId() + "-" + order.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
        // 事件埋点
        getService().confirmReleaseEvent(user, orderSn);
        return MessageResult.success(msService.getMessage("RELEASE_SUCCESS"));
    }


    /**
     * 订单放行 由BTBANK调用
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "releaseBOA")
    public MessageResult confirmReleaseBOA(String orderSn, String jyPassword, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        String jyPass = new SimpleHash("md5", jyPassword, member.getSalt(), 2).toHex().toLowerCase();
        Assert.isTrue(jyPass.equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));

        MessageRespResult<Object> objectMessageRespResult = ibtOpenApiService.releaseForV1(orderSn, member.getId());
        if (Objects.isNull(objectMessageRespResult) || !objectMessageRespResult.isSuccess()) {
            return MessageResult.error(objectMessageRespResult.getMessage());
        }
        return MessageResult.success(msService.getMessage("RELEASE_SUCCESS"));
    }


    /**
     * 订单放行 BTBANK-OTC-API 由api调用
     *
     * @param orderSn
     * @param memberId
     * @return true
     * @author shenzucai
     * @time 2019.10.01 18:38
     */
    @RequestMapping(value = "releaseBOAForApi")
    public MessageResult confirmReleaseBOAForApi(String orderSn, Long memberId) throws Exception {

        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_STATUS_EXPIRED"));
        isTrue(org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(order.getOrderSourceType(), "88888888"), msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getCustomerId().equals(memberId)) {
            //代表该会员不是广告发布者，并且是放行者
            ret = 1;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

        //edit by yangch 时间： 2018.07.31 原因：优化事务
        orderService.payOrderBOA(order, ret, memberId);
        Member member = memberService.findOne(memberId);
        //edit by yangch 时间： 2018.07.12 原因：异步推送放行订单的短信提示
        pushOrderMessageService.pushReleasedOrderMessage4SMS(order, member);
        orderEvent.onOrderCompleted(order); //订单放币完成的后续处理
        // //add by tansitao 时间： 2018/11/19 原因：订单完成，redis中的订单数减一（前提是单数不为空并且大于0）
        // redisCountorService.subtractHash(SysConstant.C2C_MONITOR_ORDER + order.getMemberId() + "-" + order.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
        // 事件埋点
        getService().confirmReleaseEvent(memberId, orderSn);
        return MessageResult.success(msService.getMessage("RELEASE_SUCCESS"));
    }


    @CollectActionEvent(collectType = CollectActionEventType.OTC_PAY_COIN, memberId = "#user.getId()", refId = "#orderSn")
    public void confirmReleaseEvent(AuthMember user, String orderSn) {
    }

    @CollectActionEvent(collectType = CollectActionEventType.OTC_PAY_COIN, memberId = "#memberId", refId = "#orderSn")
    public void confirmReleaseEvent(Long memberId, String orderSn) {
    }

    /**
     * 申诉
     *
     * @param appealApply
     * @param bindingResult
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "appeal")
//    @Transactional(rollbackFor = Exception.class)
    public MessageResult appeal(@Valid AppealApply appealApply, BindingResult bindingResult, @SessionAttribute(SESSION_MEMBER) AuthMember user) throws UnexpectedException {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        Order order = orderService.findOneByOrderSn(appealApply.getOrderSn());
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("NO_APPEAL"));

        //add by  shenzucai 时间： 2019.03.20  原因：付款完成后30分钟才可以进行申诉
        if (DateUtil.compareDateMinute(new Date(), order.getPayTime()) < 30) {
            return error(msService.getMessage("FIRST_APPEAL_ILLEGAL_TIME"));
        }
        //add by zyj 2018-12-20 : 取消申诉30分钟后才能再次申诉
        Appeal lastAppeal = appealService.findNewByorderId(String.valueOf(order.getId()));
        if (lastAppeal != null && lastAppeal.getStatus().equals(AppealStatus.CANCELED) && DateUtil.compareDateMinute(new Date(), lastAppeal.getCancelTime()) < 30) {
            return error(msService.getMessage("APPEAL_ILLEGAL_TIME"));
        }
        int ret = 0;
        if (order.getMemberId().equals(user.getId())) {
            ret = 1;
        } else if (order.getCustomerId().equals(user.getId())) {
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));

//        if (!(orderService.updateOrderAppeal(order.getOrderSn()) > 0)) {
//            throw new UnexpectedException(String.format("OTCSS001：%s", msService.getMessage("APPEAL_FAILED")));
//        }
        Appeal appeal = new Appeal();
        appeal.setInitiatorId(user.getId());
        if (ret == 1) {
            appeal.setAssociateId(order.getCustomerId());
        } else {
            appeal.setAssociateId(order.getMemberId());
        }
        appeal.setOrder(order);
        appeal.setRemark(appealApply.getRemark());
        appeal.setAppealType(appealApply.getAppealType());//add by zyj: 2018.11.1  将申诉类型存入表

//        Appeal appeal1 = appealService.save(appeal);
        Appeal appeal1 = null;
        try {
            appeal1 = appealService.dealOrderApplea(appeal, order, appealApply.getMaterialUrls());
        } catch (Exception e) {
            log.error("==========订单申诉异常,订单id:" + order.getOrderSn() + "==========", e);
        }
        if (appeal1 != null) {
            //add by tansitao 时间： 2018/9/6 原因：保存申诉材料
//            if(!StringUtils.isEmpty(appealApply.getMaterialUrls())){
//                String[] materialUrls = appealApply.getMaterialUrls().split(",");
//                for (String materialUrl:materialUrls) {
//                    OrderAppealAccessory orderAppealAccessory = new OrderAppealAccessory();
//                    orderAppealAccessory.setAppealId(appeal1.getId());
//                    orderAppealAccessory.setUrlPath(materialUrl.split("[?]")[0].split("[|/]",4)[3]);
//                    orderAppealAccessoryService.save(orderAppealAccessory);
//                }
//            }

            //add by tansitao 时间： 2018/11/19 原因：订单申诉，redis中的订单数减一（前提是单数不为空并且大于0）
            redisCountorService.subtractHash(SysConstant.C2C_MONITOR_ORDER + order.getMemberId() + "-" + order.getAdvertiseId(), SysConstant.C2C_ONLINE_NUM);
            getService().creatAppeal(order, user);
            return MessageResult.success(msService.getMessage("APPEAL_SUCCESS"));
        } else {
            throw new UnexpectedException(String.format("OTCSS002：%s", msService.getMessage("APPEAL_FAILED")));
        }
    }


    //允许小数最后一位的精度有“正负1”的误差
    public static boolean isEqualIgnoreTailPrecision(BigDecimal v1, BigDecimal v2) {
        if (v1.compareTo(v2) == 0) {
            return true;
        } else {
            //最大精度位数
            int maxScala = Math.max(v1.scale(), v2.scale());

            //精度
            BigDecimal ignorePrecision = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(Math.pow(10, maxScala)));

            if (v1.subtract(v2).abs()
                    .compareTo(ignorePrecision) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 取消订单权限冻结提示
     *
     * @param
     * @return * @param
     * @author Zhang Yanjun
     * @time 2018.11.05 16:09
     */
    @ApiOperation(value = "取消订单权限冻结提示")
    @RequestMapping(value = "frozenPermission")
    public MessageRespResult frozenPermission(@SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        //查询权限冻结表
        List<MonitorRuleConfigDto> monitorList = monitorRuleService.findMonitorRuleByEvent(MonitorTriggerEvent.OTC_CANCEL_ORDER);
        if (monitorList.size() == 0) {
            return MessageRespResult.success("没有该事件的权限冻结配置");
        }
        //该会员会触发的权限
        List<Map<String, Object>> list = new ArrayList<>();
        //与每个事件段的触发次数相差几次
        int count = 0;
        for (int i = 0; i < monitorList.size(); i++) {
            //会员等级相等的权限
            MemberLevelEnum memberLevel = monitorList.get(i).getTriggerUserLevel();
            if (member.getMemberLevel().getOrdinal() == memberLevel.getOrdinal()) {
                //该时间段内用户取消次数
                Map<String, Object> memberCancelCount = monitorRuleService.findOneByMemberId(user.getId(), monitorList.get(i).getTriggerStageCycle());
                //用户取消次数
                int memberCount = 0;
                if (memberCancelCount != null) {
                    memberCount = Integer.parseInt(memberCancelCount.get("sumCancel").toString());
                }
                //惩罚触发次数
                int triggerTimes = monitorList.get(i).getTriggerTimes();
                if (sub(triggerTimes, memberCount).compareTo(BigDecimal.valueOf(0)) == 1) {
                    count = sub(triggerTimes, memberCount).intValue();
                    Map<String, Object> map = new HashMap<>();
                    map.put("triggerStageCycle", monitorList.get(i).getTriggerStageCycle());
                    //触发剩余次数
                    map.put("count", count);
                    //冻结权限配置表
                    map.put("monitorRuleConfig", monitorList.get(i));
                    //用户已取消次数
                    map.put("memberCount", memberCount);
                    list.add(map);
                }
            }
        }
        //用户已取消次数
        int memberCount = 0;
        if (list.size() == 0) {
            return MessageRespResult.success("查询成功");
        }
        count = (int) list.get(0).get("count");
        MonitorRuleConfigDto monitorRuleConfigDto = new MonitorRuleConfigDto();
        //查找最接近的触发惩罚 最短的时间段内剩余触发次数最少的
        for (int i = 0; i < list.size(); i++) {
            if ((int) list.get(i).get("triggerStageCycle") <= (int) list.get(0).get("triggerStageCycle")) {
                count = (int) list.get(i).get("count") > count ? count : (int) list.get(i).get("count");
                memberCount = (int) list.get(i).get("memberCount");
                monitorRuleConfigDto = (MonitorRuleConfigDto) list.get(i).get("monitorRuleConfig");
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("memberCount", memberCount);
        map.put("monitorRule", monitorRuleConfigDto);
        return MessageRespResult.success("查询成功", map);
    }


    /**
     * 取消申诉
     *
     * @param type
     * @param description
     * @return
     * @author Zhang Yanjun
     * @time 2018.11.21 12:00
     */
    @ApiOperation(value = "用户取消申诉")
    @RequestMapping("cancelAppeal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appealId", value = "申诉id"),
            @ApiImplicitParam(name = "type", value = "取消原因 0已经联系上卖家，等待卖家放币，1卖家已确认到账，等待卖家放币，2买家已付款，3其他"),
            @ApiImplicitParam(name = "description", value = "取消原因描述")
    })
    public MessageRespResult cancelAppeal(Integer type, String description, Long appealId, @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        notNull(appealId, msService.getMessage("PARAMETER_ERROR"));
        Appeal appeal = appealService.findOne(appealId);
        notNull(appeal, msService.getMessage("APPEAL_NOT_HAVE"));
        isTrue(appeal.getStatus().equals(AppealStatus.NOT_PROCESSED), msService.getMessage("APPEAL_CANCEL_FAILED"));
        //add by zyj : 非申诉方不可取消申诉
        isTrue(appeal.getInitiatorId() == user.getId(), msService.getMessage("ILLEGAL_INITIATOR"));
        appeal.setCancelReason(type);
        appeal.setCancelDescription(description);
        appeal.setCancelTime(new Date());
        appeal.setStatus(AppealStatus.CANCELED);
        appeal.setCancelId(user.getId());
        appealService.save(appeal);
        //订单退回到上一个状态  已付款
        Order order = appeal.getOrder();
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        order.setStatus(OrderStatus.PAID);
        orderService.save(order);
        // 事件埋点
        getService().cancelAppeal(user, appealId.toString());
        return MessageRespResult.success("取消成功");
    }

    /**
     * 取消申诉
     *
     * @param user
     * @param appealId
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_APPEAL_ORDER, memberId = "#user.getId()", refId = "#appealId")
    public void cancelAppeal(AuthMember user, String appealId) {
    }

    /**
     *  * 用于创建订单消息推送
     *  * @author 
     *  * @time 2018/12/21 13:41 
     *  
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_ADD_ORDER, memberId = "#user.getId()", refId = "#order.getOrderSn()")
    public void creatOrderEvent(Order order, AuthMember user) {
    }

    /**
     * 订单标记已付款
     *
     * @param orderSn
     * @param user
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_PAY_CASH, memberId = "#user.getId()", refId = "#orderSn")
    public void payOrder(AuthMember user, String orderSn) {
    }

    /**
     * 创建订单申诉埋点
     *
     * @param order
     * @param user
     */
    @CollectActionEvent(collectType = CollectActionEventType.OTC_APPEAL_ORDER, memberId = "#user.getId()", refId = "#order.getOrderSn()")
    public void creatAppeal(Order order, AuthMember user) {
    }

    public OrderController getService() {
        return SpringContextUtil.getBean(OrderController.class);
    }

    /**
     * modify by qhliao 因商家购币通道需要调用故改为public
     * 排序商家最小订单，并过滤商家广告。 具体规则：<br>
     * 1. 匹配付款渠道和接单金额范围<br>
     * 2. 正在进行中的订单少的（买卖单加在一起计算，不含申诉中的）<br>
     * 3. 48小时内接单次数少的<br>
     * 4. 48小时内接单金额小的<br>
     */
    public List<Advertise> filterAvailableAdvList(List<Advertise> advertiseList, AdvertiseType type) {
        Map<Long, Long> memberCount = new HashMap<>();
        advertiseList.forEach(item -> memberCount.put(item.getBMemberId(), 0L));
        // 查询商家对应的订单数
        List<Map<String, Long>> counts = otcOrderService.selectCountByMembers(memberCount.keySet().toArray(new Long[0]), type);
        // 记录商家订单数
        counts.forEach(item -> memberCount.put(item.get("member_id"), item.getOrDefault("count", 0L)));
        // 获取订单数最小商家
        Long min = memberCount.values().stream().mapToLong(v -> v).min().orElse(0L);
        List<Long> ids = new ArrayList<>();
        memberCount.forEach((k, v) -> {
            if (v.intValue() == min.intValue()) ids.add(k);
        });

        // 查询商家48小时接单数量
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        List<Map<String, Long>> count48 = otcOrderService.selectCountByMembersAnd48(ids.toArray(new Long[0]), type, calendar.getTime());
        // 过滤48小时内，未接单的商家
        List<Long> list48 = count48.stream().map(i -> i.get("member_id")).collect(Collectors.toList());
        List<Long> list = ids.stream().filter(i -> !list48.contains(i)).collect(Collectors.toList());

        // 随机一个商家
        long memberId;
        if (list.size() > 0) {
            memberId = list.get(new Random().nextInt(list.size()));
        } else if (count48.size() > 0) {
            // 取48小时内接单量和金额最小的
            memberId = count48.get(0).get("member_id");
        } else {
            memberId = ids.get(new Random().nextInt(ids.size()));
        }
        // 过滤商家的广告
        Advertise[] array = advertiseList.stream().filter(item -> memberId == item.getBMemberId()).toArray(Advertise[]::new);
        return Arrays.asList(array);
    }

    /**
     * 测试归集
     *
     * @param orderSn
     */
    @RequestMapping("saveOtcSummarizeRecord")
    public void saveOtcSummarizeRecord(String orderSn) {
        Order order = orderService.findOneByOrderSn(orderSn);
        log.info("法币交易归集{}", orderService.saveOtcSummarizeRecord(order.getOrderSn(), order.getCommission(), order.getCoin()));
    }

}
