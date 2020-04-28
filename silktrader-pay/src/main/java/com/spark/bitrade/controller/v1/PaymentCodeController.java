package com.spark.bitrade.controller.v1;

import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PaymentCodeStrategyType;
import com.spark.bitrade.entity.Member;
import com.spark.bitrade.entity.PayPaymentCodeManage;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.service.IPayPaymentCodeManageService;
import com.spark.bitrade.service.MemberService;
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

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;


/**
 *  付款码功能控制器
 *
 * @author yangch
 * @time 2019.03.01 14:27
 */
@Api(description = "付款码功能控制器")
@RestController
@RequestMapping("/paymentCode")
@Slf4j
public class PaymentCodeController {
    @Autowired
    private IPayPaymentCodeManageService payPaymentCodeManageService;
    @Autowired
    private MemberService memberService;

    /**
     * 查询付款码功能信息
     * @return
     */
    @ApiOperation(value = "查询付款码功能信息")
    @RequestMapping(value = "/find", method={RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult find(@SessionAttribute(SESSION_MEMBER) AuthMember member){
        return MessageRespResult.success4Data(
                payPaymentCodeManageService.findByMemberIdAndAppid(member.getId(), member.getPlatform()));
    }

    /**
     * 查询付款码功能信息
     * @param accountId 帐号ID
     * @param appId 应用ID
     * @return
     */
    @ApiOperation(value = "查询付款码功能信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "帐号ID，如：手机号、邮箱",name = "accountId",dataType = "String"),
            @ApiImplicitParam(value = "应用ID",name = "appId",dataType = "String")
    })
    @RequestMapping(value = "/api/find", method={RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult find(String accountId,
                                  String appId){
        if(StringUtils.isEmpty(accountId) || StringUtils.isEmpty(appId)){
            return MessageRespResult.error(MessageCode.REQUIRED_PARAMETER);
        }

        Member member = memberService.findMemberByMobilePhoneOrEmail(accountId);
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER);
        }

        return MessageRespResult.success4Data(
                payPaymentCodeManageService.findByMemberIdAndAppid(member.getId(), appId));
    }


    /**
     * 开启付款码功能
     *
     * @param strategyType 付款码生成策略，1、定时自动刷新 2、每次用后失效
     * @param terminalDeviceInfo 终端信息，eg：{"imei","12341234143","imsi":"8974192454","phone_model":"小米","ratio":"400*500"}
     * @return
     */
    @ApiOperation(value = "开启付款码功能")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "付款码生成策略，1、定时自动刷新 2、每次用后失效",
                    name = "strategyType", dataType = "Enum"),
            @ApiImplicitParam(value = "终端信息，eg：{\"imei\",\"12341234143\",\"imsi\":\"8974192454\",\"phone_model\":\"小米\",\"ratio\":\"400*500\"}",
                    name = "terminalDeviceInfo",dataType = "String")
    })
    @RequestMapping(value = "/open", method={RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult open(@SessionAttribute(SESSION_MEMBER) AuthMember member,
                                  PaymentCodeStrategyType strategyType,
                                  String terminalDeviceInfo){
        if (strategyType == null) {
            strategyType = PaymentCodeStrategyType.CYCLE_REFRESH;
        }

        PayPaymentCodeManage entity = new PayPaymentCodeManage();
        entity.setMemberId(member.getId());
        entity.setAppId(member.getPlatform());
        entity.setStrategyType(strategyType);
        entity.setTerminalDeviceInfo(terminalDeviceInfo);
        entity.setEnabled(BooleanEnum.IS_TRUE);

        return MessageRespResult.success4Data(
                payPaymentCodeManageService.save(entity));
    }

    /**
     * 开启付款码功能
     *
     * @param accountId 帐号ID
     * @param appId  应用ID
     * @param strategyType 付款码生成策略，1、定时自动刷新 2、每次用后失效
     * @param terminalDeviceInfo 终端信息，eg：{"imei","12341234143","imsi":"8974192454","phone_model":"小米","ratio":"400*500"}
     * @return
     */
    @ApiOperation(value = "开启付款码功能API")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "帐号ID，如：手机号、邮箱",name = "accountId",dataType = "String"),
            @ApiImplicitParam(value = "应用ID",name = "appId",dataType = "String"),
            @ApiImplicitParam(value = "付款码生成策略，1、定时自动刷新 2、每次用后失效",
                    name = "strategyType", dataType = "Enum"),
            @ApiImplicitParam(value = "终端信息，eg：{\"imei\",\"12341234143\",\"imsi\":\"8974192454\",\"phone_model\":\"小米\",\"ratio\":\"400*500\"}",
                    name = "terminalDeviceInfo",dataType = "String")
    })
    @RequestMapping(value = "/api/open", method={RequestMethod.GET, RequestMethod.POST})
    public MessageRespResult open(String accountId,
                                  String appId,
                                  PaymentCodeStrategyType strategyType,
                                  String terminalDeviceInfo){
        if(StringUtils.isEmpty(accountId) || StringUtils.isEmpty(appId)
                || StringUtils.isEmpty(strategyType)){
            return MessageRespResult.error(MessageCode.REQUIRED_PARAMETER);
        }

        Member member = memberService.findMemberByMobilePhoneOrEmail(accountId);
        if (member == null) {
            return MessageRespResult.error(MessageCode.MISSING_USER);
        }

        PayPaymentCodeManage entity = new PayPaymentCodeManage();
        entity.setMemberId(member.getId());
        entity.setAppId(appId);
        entity.setStrategyType(strategyType);
        entity.setTerminalDeviceInfo(terminalDeviceInfo);
        entity.setEnabled(BooleanEnum.IS_TRUE);

        return MessageRespResult.success4Data(
                payPaymentCodeManageService.save(entity));
    }
}
