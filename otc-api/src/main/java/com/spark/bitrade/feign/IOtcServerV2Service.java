package com.spark.bitrade.feign;


import com.alibaba.fastjson.JSONObject;
import com.spark.bitrade.entity.CurrencyManage;
import com.spark.bitrade.entity.PaySetting;
import com.spark.bitrade.util.MessageRespResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: ss
 * @date: 2020/3/24
 */
@FeignClient("otc-server")
public interface IOtcServerV2Service {
    /**
     * 根据用户ID获取默认法币设置
     * @param memberId
     * @return
     */
    @PostMapping(
            value = "/otcServerApi/api/v2/currencyManage/getMemberCurrencyByMemberId",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<CurrencyManage> getMemberCurrencyByMemberId(@RequestParam("memberId") Long memberId);
    /**
     * 根据币种获取法币价格
     * @param fSymbol 法币币种ID
     * @param tSymbol 交易币种缩写
     * @return
     */

    @PostMapping(
            value = "/otcServerApi/api/v2/otcCoin/getCurrencyPrice",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<BigDecimal> getCurrencyRate(@RequestParam("fSymbol") Long fSymbol,
                                             @RequestParam("tSymbol") String tSymbol);

    /**
     * 获取用户支付配置信息
     * @param memberId
     * @return
     * @author ss
     * @since 2020年3月28日
     */
    @PostMapping(value = "/otcServerApi/api/v2/memberCapSetting/queryCapSettingByMemberId",consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<JSONObject> queryCapSettingByMember(@RequestParam("memberId")Long memberId);

    /**
     * 获取当前系统可用的支付方式
     * @return
     * @param currencyId 法币id，如果不指定，则返回全部支付方式，指定时，返回指定法币绑定的支付方式
     * @author ss
     * @since 2020年3月18日
     */
    @PostMapping(value = "/otcServerApi/api/v2/paySetting/getPaySettings",consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<List<PaySetting>> getPaySettings(@RequestParam("baseId")Long currencyId);

    /**
     * 根据法币ID获取法币信息(内部接口供v1的otc调用)
     * @param currencyId 法币ID
     * @return
     */
    @PostMapping(value = "/otcServerApi/api/v2/currencyManage/getCurrencyById" ,consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<CurrencyManage> getCurrencyById(@RequestParam("currencyId")Long currencyId);

    /**
     * 全部法币列表
     * @return
     */
    @PostMapping(value = "/otcServerApi/api/v2/currencyManage/no-auth/getAllCurrency" ,consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<List<CurrencyManage>> getAllCurrency();

    /**
     * 根据配置key获取配置value
     * @param ruleKey 配置key
     * @return
     */
    @PostMapping(value = "/otcServerApi/api/v2/currencyRuleSetting/getCurrencyRuleValueByKey" ,consumes = {MediaType.APPLICATION_JSON_VALUE})
    MessageRespResult<String> getCurrencyRuleValueByKey(@RequestParam("ruleKey")String ruleKey);

}
