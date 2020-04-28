package com.spark.bitrade.controller;

import com.spark.bitrade.constant.ExchangeOrderDirection;
import com.spark.bitrade.entity.ExchangeFastCoinDO;
import com.spark.bitrade.entity.ExchangeFastOrderDO;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.exception.MessageCodeException;
import com.spark.bitrade.feign.ICoinExchange;
import com.spark.bitrade.service.IExchangeFastCoinService;
import com.spark.bitrade.service.IExchangeFastOrderService;
import com.spark.bitrade.util.AssertUtil;
import com.spark.bitrade.util.MessageRespResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 *  
 * 闪兑功能
 *
 * @author yangch
 * @time 2019.03.27 09:50
 */

@Slf4j
@RestController
@RequestMapping("/fast")
@Api(description = "闪兑功能")
public class ExchangeFastController {
    @Autowired
    private IExchangeFastCoinService fastCoinService;
    @Autowired
    private IExchangeFastOrderService fastOrderService;

    @Autowired
    private ICoinExchange coinExchange;


    /**
     * 闪兑支持币种的列表接口
     *
     * @param appId
     * @return
     */
    @ApiOperation(value = "闪兑支持币种的列表接口", notes = "闪兑支持币种的列表接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "app端的ID", name = "appId", required = true),
            @ApiImplicitParam(value = "闪兑基币币种名称，如CNYT、BT", name = "baseSymbol")
    })
    @RequestMapping(value = "/support/coins",
            method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<List<ExchangeFastCoinDO>> supportCoins(String appId, String baseSymbol) {
        if (StringUtils.hasText(baseSymbol)) {
            baseSymbol = baseSymbol.toUpperCase();
        }
        return MessageRespResult.success4Data(fastCoinService.list4CoinSymbol(appId, baseSymbol));
    }

    /**
     * 闪兑基币币种的列表接口
     *
     * @param appId
     * @return
     */
    @ApiOperation(value = "闪兑基币币种的列表接口", notes = "闪兑基币币种的列表接口")
    @ApiImplicitParam(value = "app端的ID", name = "appId", required = true)
    @RequestMapping(value = "/support/baseCoins",
            method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<List<String>> supportBaseCoins(String appId) {
        return MessageRespResult.success4Data(fastCoinService.list4BaseSymbol(appId));
    }


    /**
     * 闪兑接口
     *
     * @param coinSymbol
     * @param baseSymbol
     * @param amount
     * @return
     */
    @ApiOperation(value = "闪兑下单接口(依赖会话)", notes = "闪兑下单接口(依赖会话)")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "闪兑币种名称，如BTC、LTC", name = "coinSymbol", required = true),
            @ApiImplicitParam(value = "闪兑基币币种名称，如CNYT、BT", name = "baseSymbol", required = true),
            @ApiImplicitParam(value = "闪兑数量", name = "amount", required = true),
            @ApiImplicitParam(value = "订单方向:0=买入(闪兑基币->闪兑币)/1=卖出(闪兑币->闪兑基币)", name = "direction", required = true)
    })
    @RequestMapping(value = "/addOrder",
            method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeFastOrderDO> addOrder(@ApiIgnore() @SessionAttribute(SESSION_MEMBER) AuthMember member, String coinSymbol, String baseSymbol,
                                                           BigDecimal amount, ExchangeOrderDirection direction) {
        return addOrderApi(member.getId(), member.getPlatform(), coinSymbol, baseSymbol, amount, direction);
    }

    /**
     * 闪兑下单接口
     *
     * @param memberId   会员ID
     * @param appId      应用ID
     * @param coinSymbol 闪兑币种名称
     * @param baseSymbol 基币名称
     * @param amount     闪兑数量
     * @param direction  兑换方向
     * @return
     */
    @ApiOperation(value = "闪兑下单接口", notes = "闪兑下单接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "会员ID", name = "memberId", required = true),
            @ApiImplicitParam(value = "应用ID", name = "appId", required = true),
            @ApiImplicitParam(value = "闪兑币种名称，如BTC、LTC", name = "coinSymbol", required = true),
            @ApiImplicitParam(value = "闪兑基币币种名称，如CNYT、BT", name = "baseSymbol", required = true),
            @ApiImplicitParam(value = "闪兑数量", name = "amount", required = true),
            @ApiImplicitParam(value = "订单方向:0=买入(闪兑基币->闪兑币)/1=卖出(闪兑币->闪兑基币)", name = "direction", required = true)
    })
    @RequestMapping(value = "/api/addOrder",
            method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeFastOrderDO> addOrderApi(Long memberId, String appId,
                                                              String coinSymbol, String baseSymbol,
                                                              BigDecimal amount, ExchangeOrderDirection direction) {
        //币种大小写兼容
        if (StringUtils.hasText(coinSymbol)) {
            coinSymbol = coinSymbol.toUpperCase();
        }
        if (StringUtils.hasText(baseSymbol)) {
            baseSymbol = baseSymbol.toUpperCase();
        }

        //获取兑换汇率
        BigDecimal currentPrice = this.exchangeFastRate(appId, coinSymbol, baseSymbol, direction);
        AssertUtil.isTrue(currentPrice.compareTo(BigDecimal.ZERO) > 0, MessageCode.INVALID_EXCHANGE_RATE);

        ExchangeFastOrderDO order = fastOrderService.exchangeInitiator(memberId, appId, coinSymbol,
                baseSymbol, amount, direction, currentPrice);

        return MessageRespResult.success4Data(order);
    }

    /**
     * 闪兑下单接口
     *
     * @param orderId 订单ID
     * @return
     */
    @ApiOperation(value = "闪兑订单接收方重做接口", notes = "闪兑订单接收方重做接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "闪兑订单ID", name = "orderId", required = true)
    })
    @RequestMapping(value = "/api/redoExchangeReceiver",
            method = {RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult<ExchangeFastOrderDO> redoExchangeReceiver(Long orderId) {
        AssertUtil.notNull(orderId, MessageCode.INVALID_PARAMETER);

        fastOrderService.exchangeReceiver(orderId);

        return MessageRespResult.success();
    }

    /**
     * 闪兑汇率接口
     *
     * @param coinSymbol 闪兑币种
     * @param baseSymbol 闪兑基币
     * @param direction  兑换方向
     * @return
     */
    @ApiOperation(value = "闪兑汇率接口", notes = "闪兑汇率接口")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用ID", name = "appId"),
            @ApiImplicitParam(value = "闪兑币种名称，如BTC、LTC", name = "coinSymbol", required = true),
            @ApiImplicitParam(value = "闪兑基币币种名称，如CNYT、BT", name = "baseSymbol", required = true),
            @ApiImplicitParam(value = "订单方向:0=买入(闪兑基币->闪兑币)/1=卖出(闪兑币->闪兑基币)", name = "direction", required = true)
    })
    @RequestMapping(value = "/exchangeRate",
            method = {RequestMethod.GET, RequestMethod.POST})
    public BigDecimal exchangeFastRate(String appId, String coinSymbol, String baseSymbol, ExchangeOrderDirection direction) {
        // 校验输入参数
        AssertUtil.notNull(coinSymbol, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(baseSymbol, MessageCode.INVALID_PARAMETER);
        AssertUtil.notNull(direction, MessageCode.INVALID_PARAMETER);

        //币种大小写兼容
        if (StringUtils.hasText(coinSymbol)) {
            coinSymbol = coinSymbol.toUpperCase();
        }
        if (StringUtils.hasText(baseSymbol)) {
            baseSymbol = baseSymbol.toUpperCase();
        }

        // 验证币种是否支持闪兑
        ExchangeFastCoinDO exchangeFastCoin = fastCoinService.findByAppIdAndCoinSymbol(appId, coinSymbol, baseSymbol);
        AssertUtil.notNull(exchangeFastCoin, MessageCode.NONSUPPORT_FAST_EXCHANGE_COIN);

        //获取汇率
        BigDecimal coinRate = exchangeFastCoin.getCoinSymbolFixedRate();
        BigDecimal baseRate = exchangeFastCoin.getBaseSymbolFixedRate();

        if (!this.isValidFixedRate(coinRate)) {
            log.info("未配置coinSymbolFixedRate，从市场行情获取汇率");
            coinRate = coinExchange.getCnytExchangeRate(
                    fastCoinService.getRateValidCoinSymbol(exchangeFastCoin)).getData();
        }
        if (!this.isValidFixedRate(baseRate)) {
            log.info("未配置baseSymbolFixedRate，从市场行情获取汇率");
            baseRate = coinExchange.getCnytExchangeRate(
                    fastCoinService.getRateValidBaseSymbol(exchangeFastCoin)).getData();
        }


        if (direction == ExchangeOrderDirection.BUY) {
            //买入场景： 兑换基币币种 -> 接收币种
            //      基币(BT)=1 -> 兑换币(BTC)=4， 汇率计算 = currentPrice= BT/BTC=1/4=0.25
            if (coinRate.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            } else {
                return baseRate.multiply(BigDecimal.ONE.subtract(exchangeFastCoin.getBuyAdjustRate()))
                        .divide(coinRate, 16, BigDecimal.ROUND_DOWN);
            }
        } else {
            //卖出场景：兑换币 ->基币
            //      兑换币(BTC)=4 -> 基币(BT)=1， 汇率计算 = currentPrice= BTC/BT=4/1=4
            if (baseRate.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            } else {
                return coinRate.multiply(BigDecimal.ONE.subtract(exchangeFastCoin.getSellAdjustRate()))
                        .divide(baseRate, 16, BigDecimal.ROUND_DOWN);
            }
        }
    }

    /**
     * 判断是否为有效的的固定汇率
     *
     * @param fixedRate 汇率
     * @return
     */
    private boolean isValidFixedRate(BigDecimal fixedRate) {
        if (StringUtils.isEmpty(fixedRate)) {
            return false;
        }
        if (fixedRate.compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return false;
    }
}
