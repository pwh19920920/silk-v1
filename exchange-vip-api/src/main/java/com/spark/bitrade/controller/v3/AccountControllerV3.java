package com.spark.bitrade.controller.v3;

import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.controller.ApiCommonController;
import com.spark.bitrade.entity.MemberWallet;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.feign.ICywService;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 *  <p>rest 交易账户api</p>
 *
 * @author yangch
 * @time 2019.03.02 13:53
 */

@Slf4j
@RestController
@RequestMapping("/v3/account")
public class AccountControllerV3 extends ApiCommonController {
    @Autowired
    private ICywService cywService;

    /**
     * 获取用户账户信息
     *
     * @param request HttpServletRequest
     * @param symbol  币种代码，"BTC","ETH"...
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @RequestMapping(value = "/contract_account_info", method = {RequestMethod.GET, RequestMethod.POST})
    public MessageResult accountInfo(HttpServletRequest request,
                                     @RequestParam(name = "symbol") String symbol) {
        log.info("symbol params={}", symbol);
        //用户信息
        AuthMember member = super.getAuthMember(request);
        MessageRespResult<BigDecimal> respResult = cywService.balance(member.getId(), symbol.toUpperCase());
        if (respResult.isSuccess()) {
            MemberWallet memberWallet = new MemberWallet();
            memberWallet.setId(member.getId());
            memberWallet.setMemberId(member.getId());
            memberWallet.setBalance(respResult.getData());
            memberWallet.setFrozenBalance(BigDecimal.ZERO);
            memberWallet.setLockBalance(BigDecimal.ZERO);

            return MessageResult.success(respResult.getMessage(), memberWallet);
        } else {
            return respResult.to();
        }

    }
}
