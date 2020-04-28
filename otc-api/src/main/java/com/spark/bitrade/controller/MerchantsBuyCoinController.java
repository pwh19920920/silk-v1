package com.spark.bitrade.controller;

import com.spark.bitrade.coin.CoinExchangeFactory;
import com.spark.bitrade.config.TradeConfig;
import com.spark.bitrade.constant.*;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.exception.UnexpectedException;
import com.spark.bitrade.feign.IOtcServerV2Service;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.vo.MQutoesVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.spark.bitrade.constant.PayMode.*;
import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.BigDecimalUtils.*;
import static com.spark.bitrade.util.MessageResult.error;
import static org.springframework.util.Assert.isTrue;

/**
 *  
 *    
 *  @author liaoqinghui  
 *  @time 2019.07.12 09:28  
 */
@Api(description = "商家购币通道")
@RestController
@RequestMapping(value = "/order")
@Slf4j
public class MerchantsBuyCoinController {


    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private TradeConfig tradeConfig;
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private IOtcServerV2Service iOtcServerV2Service;
    @Autowired
    private MerchantBuyConfigService merchantBuyConfigService;
    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private BusinessDiscountRuleService businessDiscountRuleService;
    @Autowired
    private MemberPaymentAccountService memberPaymentAccountService;
    @Autowired
    private PushOrderMessageService pushOrderMessageService;
    @Autowired
    private RedisCountorService redisCountorService;
    @Autowired
    private ISilkDataDistService silkDataDistService;
    @Autowired
    private ValidateOpenTranscationService validateOpenTranscationService;

    /**
     *  * 获取报价
     *  * @author tansitao
     *  * @time 2019/1/3 14:10 
     *  
     */
    @ApiOperation(value = "获取报价")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinUnit", value = "币种"),
            @ApiImplicitParam(name = "amount", value = "数量"),
            @ApiImplicitParam(name = "pays", value = "支付方式")
    })
    @RequestMapping(value = "getQuotes", method = RequestMethod.POST)
    public MessageResult getQuotes(String coinUnit, BigDecimal amount, String[] pays,
                                   @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {

        //验证支付方式
        Assert.notEmpty(pays, msService.getMessage("MISSING_PAY"));

        //获取币种信息
        OtcCoin otcCoin = otcCoinService.findByUnit(coinUnit);
        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());
        //add by ss 获取用户默认法币
        MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
        isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
        //edit by ss 获取价格
//        BigDecimal marketPrice = coins.get(otcCoin.getUnit());
        BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(currencyManageMessageRespResult.getData().getId(),otcCoin.getUnit()).getData();
        Advertise advertise = filterAdvertise(otcCoin, amount, user.getId(), pays, marketPrice, appId,currencyManageMessageRespResult.getData().getId());
        MQutoesVo vo = calculatePrice(advertise, otcCoin, marketPrice, amount);
        return MessageResult.success("获取报价成功", vo);
    }


    /**
     *  * 一键买币
     *  * @author tansitao
     *  * @time 2019/1/3 14:10 
     *  
     */
    @ApiOperation(value = "商家购币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinUnit", value = "币种"),
            @ApiImplicitParam(name = "amount", value = "数量"),
            @ApiImplicitParam(name = "pays", value = "支付方式"),
            @ApiImplicitParam(name = "advertiseId", value = "广告ID")
    })
    @RequestMapping(value = "merchantBuy", method = RequestMethod.POST)
    public MessageResult merchantBuy(String coinUnit, BigDecimal amount,
                                     String[] pays,
                                     Long advertiseId,
                                     @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                     HttpServletRequest request) throws UnexpectedException {
        //add by ss 不支持USDC
        isTrue(!"USDC".equalsIgnoreCase(coinUnit),"不支持币种：USDC");
        //验证支付方式
        Assert.notEmpty(pays, msService.getMessage("MISSING_PAY"));
        //购买方
        Member member = memberService.findOne(user.getId());

        //购买限制
        if (1 == tradeConfig.getIsOpenLimit()) {
            isTrue(orderService.isAllowTrade(user.getId(), tradeConfig.getOrderNum()), msService.getMessage("NO_ALLOW_TRADE"));
            isTrue(StringUtils.isEmpty(member.getTransactionStatus()) || member.getTransactionStatus()
                    == BooleanEnum.IS_TRUE, msService.getMessage("NO_ALLOW_TRANSACT"));
            validateOpenTranscationService.validateOpenExPitTransaction(member.getId(),
                    msService.getMessage("NO_ALLOW_TRANSACT_BUY"), AdvertiseType.BUY);
        }
        String appId = AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform());
        //获取币种信息
        OtcCoin otcCoin = otcCoinService.findByUnit(coinUnit);
        //add by ss 获取用户默认法币
        MessageRespResult<CurrencyManage> currencyManageMessageRespResult = iOtcServerV2Service.getMemberCurrencyByMemberId(user.getId());
        isTrue(currencyManageMessageRespResult.isSuccess(), currencyManageMessageRespResult.getMessage());
        //edit by ss 获取价格
//        BigDecimal marketPrice = coins.get(otcCoin.getUnit());
        BigDecimal marketPrice = iOtcServerV2Service.getCurrencyRate(currencyManageMessageRespResult.getData().getId(),otcCoin.getUnit()).getData();
        //用之前报价的广告，如果下架了或者 不存在再另外查询
        Advertise advertise = advertiseService.findById(advertiseId);
        if (advertise == null || (advertise != null && advertise.getStatus() != AdvertiseControlStatus.PUT_ON_SHELVES)) {
            advertise = filterAdvertise(otcCoin, amount, user.getId(), pays, marketPrice, appId,currencyManageMessageRespResult.getData().getId());
        } else {
            advertise.setCommission(calculateCommission(advertise, otcCoin, amount));
        }
        //风控 商家ID
        Member bMember = memberService.findOne(advertise.getBMemberId());
        advertise.setMember(bMember);
        //add by zyj 2018.12.27 : 接入风控
        TradeCashInfo tradeCashInfo = new TradeCashInfo();
        tradeCashInfo.setDirection(0);
        tradeCashInfo.setCoin(coinUnit);
        tradeCashInfo.setAmount(amount);
        tradeCashInfo.setTargetUser(memberService.findOne(advertise.getBMemberId()));

        MessageResult res = getOrderController().risk(request, null, member, tradeCashInfo);
        if (res.getCode() != 0) {
            return error(res.getMessage());
        }
        //创建订单

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
        //modify by qhliao 20190827 商家购币不收取手续费
        order.setCommission(BigDecimal.ZERO);
        //del by ss
        //order.setCountry(advertise.getCountryName());
        //add by ss
        order.setCurrencyId(currencyManageMessageRespResult.getData().getId());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(caccountName);
        order.setMemberId(bMember.getId());
        order.setMemberName(bMember.getUsername());
        order.setMemberRealName(baccountName);
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        MQutoesVo vo = calculatePrice(advertise, otcCoin, marketPrice, amount);
        order.setPrice(vo.getSinglePrice());
        BigDecimal idiscount = getIdiscount(coinUnit);

        order.setMoney(vo.getTotalPrice().subtract(vo.getTotalPrice().multiply(idiscount)));

        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setTimeLimit(advertise.getTimeLimit());
        order.setPayCode(randomCode);


        // 买币服务费默认填充
        order.setOrderMoney(vo.getTotalPrice());
        order.setServiceRate(new BigDecimal(0));
        order.setServiceMoney(new BigDecimal(0));

        order.setOrderSourceType(AppIdUtil.getRealAppIdBySessionPaltform(user.getPlatform()));
        //设置类型为商家购币通道
        order.setIsMerchantsBuy(BooleanEnum.IS_TRUE);
//del by ss
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

            getOrderController().creatOrderEvent(order1, user);
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn().toString());
            return result;
        } else {
            throw new UnexpectedException(String.format("OTCGM002：%s", msService.getMessage("CREATE_ORDER_FAILED")));
        }

    }

    /**
     * 获取报价
     *
     * @param advertise
     * @param otcCoin
     * @param marketPrice
     * @param amount
     * @return
     */
    private MQutoesVo calculatePrice(Advertise advertise, OtcCoin otcCoin, BigDecimal marketPrice, BigDecimal amount) {
        MQutoesVo vo = new MQutoesVo();
        if (advertise.getPriceType() == PriceType.MUTATIVE) {
            BigDecimal premiseRate = advertise.getPremiseRate().divide(new BigDecimal(100),
                    otcCoin.getCoinScale(), BigDecimal.ROUND_HALF_UP);
            if (advertise.getAdvertiseType() == AdvertiseType.SELL) {
                premiseRate = BigDecimal.ONE.add(premiseRate);
            } else {
                premiseRate = BigDecimal.ONE.subtract(premiseRate);
            }
            BigDecimal price = mulRound(premiseRate, marketPrice, otcCoin.getCoinScale());
            vo.setSinglePrice(price);
            vo.setTotalPrice(price.multiply(amount));
        } else {
            vo.setSinglePrice(advertise.getPrice());
            vo.setTotalPrice(advertise.getPrice().multiply(amount));
        }
        vo.setAdvertiseId(advertise.getId());
        return vo;
    }


    /**
     * 根据币种和金额筛选商家
     *
     * @param otcCoin
     * @param amount
     * @return
     */
    private Advertise filterAdvertise(OtcCoin otcCoin, BigDecimal amount,
                                      long buyUserId, String[] pays, BigDecimal marketPrice, String appId,Long currencyId) {
        List<Advertise> advertises = new ArrayList<>();
        //获取币种信息
        isTrue(otcCoin != null, msService.getMessage("COIN_ILLEGAL"));
        //获取后台配置的商家
        List<MerchantBuyConfig> merchantBuyConfigs = merchantBuyConfigService.findByCoinUnit(otcCoin.getUnit(), appId);
        isTrue(!CollectionUtils.isEmpty(merchantBuyConfigs), msService.getMessage("NOT_EXIST_MERCHANTBUYCONFIGS"));
        List<Long> memberIds = merchantBuyConfigs.stream().map(b -> b.getMemberId()).collect(Collectors.toList());
        //查询商家的广告
        List<Advertise> advertiseList = advertiseService.getByMemberIds(memberIds, amount, marketPrice, AdvertiseType.SELL, otcCoin.getId(),currencyId);
        isTrue(advertiseList != null && advertiseList.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        //过滤广告
        for (Advertise advertise : advertiseList) {
            BigDecimal commission = calculateCommission(advertise, otcCoin, amount);
            BigDecimal buyAmount = add(commission, amount);
            //判断剩余广告数量是否大于购买数量和书续费收了之和
            if (advertise.getRemainAmount().compareTo(buyAmount) >= 0) {
                advertise.setCommission(commission);
                //判断广告商是否为自己
                if (buyUserId != advertise.getBMemberId()) {
                    //判断广告支付方式是否包含用户选择的支付方式
                    for (String pay : pays) {
                        if (advertise.getPayMode().contains(pay)) {
                            advertises.add(advertise);
                            break;
                        }
                    }
                }
            }


        }

        //如果没有可用广告抛出无广告提示
        isTrue(advertises.size() > 0, msService.getMessage("NOT_EXIST_ADVERTISE"));
        advertises = getOrderController().filterAvailableAdvList(advertises, AdvertiseType.SELL);

        //随机选择一个满足条件的广告
        Advertise advertise = advertises.get(new Random().nextInt(advertises.size()));
        return advertise;
    }

    /**
     * 计算手续费
     *
     * @param advertise
     * @param otcCoin
     * @param amount
     * @return
     */
    private BigDecimal calculateCommission(Advertise advertise,
                                           OtcCoin otcCoin,
                                           BigDecimal amount) {

        //计算手续费
        BigDecimal commission = mulRound(amount, getRate(otcCoin.getJyRate()));
        //手续费折扣率
        BigDecimal feeDiscount = BigDecimal.ZERO;
        if (otcCoin.getFeeSellDiscount().compareTo(BigDecimal.ONE) >= 0) {
            feeDiscount = commission;
        } else {
            feeDiscount = commission.multiply(otcCoin.getFeeSellDiscount());
            BigDecimal remainingFee = commission.subtract(feeDiscount); //优惠后的当前手续费
            //计算 当前会员可优惠手续费数量
            BigDecimal memberFeeDiscount = remainingFee.multiply(
                    businessDiscountRuleService.getDiscountRule(advertise.getBMemberId(),
                            otcCoin.getUnit()).getSellDiscount());
            feeDiscount = feeDiscount.add(memberFeeDiscount).setScale(8, BigDecimal.ROUND_DOWN);
        }
        commission = commission.subtract(feeDiscount);
        return commission;

    }


    private BigDecimal getIdiscount(String coinUnit) {
        SilkDataDist silkDataDist = silkDataDistService.findByIdAndKey("MERCHANT_BUY_SERVICE_RATE", coinUnit);
        Assert.notNull(silkDataDist, msService.getMessage("GLOBAL_CONFIG_IS_NOT_FIND"));
        return new BigDecimal(silkDataDist.getDictVal());
    }


    /**
     * 获取之前的orderController调用部分方法
     *
     * @return
     */
    public OrderController getOrderController() {
        return SpringContextUtil.getBean(OrderController.class);
    }

}






















