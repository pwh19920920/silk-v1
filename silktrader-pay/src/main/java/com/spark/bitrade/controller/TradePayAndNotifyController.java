package com.spark.bitrade.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.dto.NotifyWalletQRPayParameterDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.*;
import com.spark.bitrade.utils.RSAFullUtils;
import com.spark.bitrade.utils.SingalGsonUtil;
import com.spark.bitrade.vo.OrderVo;
import com.spark.bitrade.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.spark.bitrade.util.MessageRespResult.error;


/**
 * <p>种子商城</p>
 *
 * @author daring5920
 * @date 2019/1/8.
 */
@RestController
@RequestMapping("/notify/business/api")
@Slf4j
public class TradePayAndNotifyController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private IWalletQRPayService walletQRPayService;

	@Autowired
	private OptimizationPayWalletNewService payWalletService;
	@Autowired
	private MemberWalletService memberWalletService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private IPayWalletPlatMemberBindService iPayWalletPlatMemberBindService;

	@Autowired
	private IAsyncNotification iAsyncNotification;

	@Autowired
	private IAsyncNotificationBusinessService iAsyncNotificationBusinessService;
	@Autowired
	private ValidateOpenTranscationService validateOpenTranscationService;
	@Autowired
	private LocaleMessageSourceService sourceService;

	@Autowired
	private MemberSecuritySetService memberSecuritySetService;

	@Autowired
	private ApplicationContext context;

	@Value("${transfer.unable:true}")
	private Boolean transferUnable;

    @Value("${etg.account:406400}")
	private Long etgAccount;

    @Value("${etg.publicKey:MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAloyUYOfJH4XP4+Sz56nW0My3/UZNKbzsrOxUg9pP+CPkCu2bHHq/3NGQuQTykJEgGj+8VX3dztveE6whTDgoD8KZz6xkZ5BR98IznZww/3bRfgiwcoSViXls1/0vVzcGx3vpVgfUbyJDVXz9mR1NCbmWIaFFd/2+A4P7loYbJEyitTrl/Oc5gqb12m0x0vzF1zo70RQr8c4TvqNKet1w/SmK3OC5D8IdZweBO20MCiQeMGAPGlHaIDBCXCyTi1iOJXuTvJd3k6iULYE0fnqZaI8/mxcmhLIUlA4Zj6jhpdP733LTMR3DrK5u4o8b2vXUzWGpRHVz3H7jVdFAGhMu2wIDAQAB}")
    private String etgPublickey;
	/**
	 * 扫码支付
	 *
	 * @param dto     WalletQRPayParameterDTO
	 * @param request
	 * @return
	 */
	@ApiRequestLimit(count = 10000)
	@PostMapping("/gateway")
	public MessageResult gateway(@RequestBody NotifyWalletQRPayParameterDTO dto, HttpServletRequest request) {
		log.info(" geteway {}", dto);

		/**
		 * 1.参数校验
		 */
		try {
			walletQRPayService.validationArgs(dto);
		} catch (ApiException ae) {
			return MessageResult.error(ae.getMessage());
		} catch (IllegalStateException ie) {
			return MessageResult.error(ie.getMessage());
		}

		if(!StringUtils.equalsIgnoreCase("BT",dto.getSymbol())){
            return MessageResult.error("BT专用，请勿非法使用");
        }
		log.info(" trans {}", dto);

		if(transferUnable) {
			// TODO 业务暂时调整，禁止该业务
			return MessageResult.error("业务暂时无法使用");
		}
		/**
		 * 2.远程转账服务
		 */

        MessageRespResult<PayFastRecord> messageResResult = platformTransfer(Long.valueOf(dto.getFrom())
                , dto.getTo()
                , dto.getSymbol()
                , dto.getAmount()
                , request
                , PayTransferType.PAY_FAST
                , dto.getFromAppId()
                , dto.getToAppId()
                , dto.getTag()
        );

        // 记录业务关联信息，并进行异步通知
        iAsyncNotification.asyncNotification(dto.getFromAppId()
                , dto.getOrderId()
                , messageResResult.getCode() == 0 ? messageResResult.getData().getTradeSn() : null
                , messageResResult.getCode() == 0 ? "ok" : messageResResult.getMessage()
                , dto.getTag()
        );
        MessageResult messageResult = new MessageResult(messageResResult.getCode(), messageResResult.getMessage(), messageResResult.getData());
        log.info(" trans {}", messageResult);
        return messageResult;

    }

    private boolean notProdEnvironment() {
        return !"prod".equals(context.getEnvironment().getActiveProfiles()[0]);
    }


    /**
     * 扫码支付
     *
     * @param postVo  WalletQRPayParameterDTO
     * @param request
     * @return
     */
    @ApiRequestLimit(count = 10000)
    @PostMapping("/etgscGateway")
    public MessageResult etgscGateway(@RequestBody PostVo postVo, HttpServletRequest request) {

        log.info(" etgscGateway postVo {}", postVo);

        if (Objects.isNull(postVo)) {
            return MessageResult.error("参数不能为空");
        }


        // 签名验证
        if (!RSAFullUtils.doCheck(postVo.getData() + postVo.getTime().toString(), postVo.getSign(), etgPublickey)) {
            return MessageResult.error("签名校验失败");
        }

        if (System.currentTimeMillis() - postVo.getTime() > 1000 * 60) {
            return MessageResult.error("数据已失效");
        }

        NotifyWalletQRPayParameterDTO dto = SingalGsonUtil.getGson().fromJson(RSAFullUtils.decryptPub(postVo.getData(), etgPublickey), NotifyWalletQRPayParameterDTO.class);

        if(!StringUtils.equalsIgnoreCase("BT",dto.getSymbol())){
            return MessageResult.error("BT专用，请勿非法使用");
        }

        if (!notProdEnvironment()) {
            if (!StringUtils.equalsIgnoreCase("24984705", dto.getFromAppId()) || !StringUtils.equalsIgnoreCase("24984705", dto.getFromAppId())) {

            }
        }
        log.info(" etgscGateway dto {}", dto);
        /**
         * 2.远程转账服务
         */

        MessageRespResult<PayFastRecord> messageResResult = platformTransfer(etgAccount
                , dto.getTo().trim()
                , dto.getSymbol()
                , dto.getAmount()
                , PayTransferType.PAY_FAST
                , dto.getFromAppId()
                , dto.getToAppId()
                , dto.getTag()
        );

        // 记录业务关联信息，并进行异步通知
        iAsyncNotification.asyncNotification(dto.getFromAppId()
                , dto.getOrderId()
                , messageResResult.getCode() == 0 ? messageResResult.getData().getTradeSn() : null
                , messageResResult.getCode() == 0 ? "ok" : messageResResult.getMessage()
                , dto.getTag()
        );

        PayFastRecord data = messageResResult.getData();
        OrderVo orderVo = new OrderVo();
        if (!Objects.isNull(data)) {
            BeanUtils.copyProperties(data, orderVo);
        }
        MessageResult messageResult = new MessageResult(messageResResult.getCode(), messageResResult.getMessage(), orderVo);
        log.info(" trans {}", messageResult);
        return messageResult;

    }


    /**
     * 查询业务订单
     *
     * @param orderId
     * @param sign
     * @return true
     * @author shenzucai
     * @time 2019.07.30 10:55
     */
    @ApiRequestLimit(count = 10000)
    @GetMapping("/get")
    public MessageResult getBusinessOrderInfo(String orderId, String sign, Integer type) {
        // type = 0 表示买入，1 表示卖出
        if (Objects.isNull(orderId) || Objects.isNull(sign)) {
            return MessageResult.error("参数不能为空");
        }

        if (Objects.isNull(type)) {
            type = 0;
        }
        log.info(" 查询信息为： {}, {}", orderId, sign);
        /**
         * 1.参数校验
         */
        String tag = null;
        String valiSign = MD5Util.md5Encode(orderId + "zzsc2019");

        if (StringUtils.equalsIgnoreCase(valiSign, sign)) {
            tag = "zzsc";
        } else {
            valiSign = MD5Util.md5Encode(orderId + "EtgSc2019");
            if (StringUtils.equalsIgnoreCase(valiSign, sign)) {
                tag = "etgsc";
            } else {
                valiSign = MD5Util.md5Encode(orderId + "paid2019");
                if (StringUtils.equalsIgnoreCase(valiSign, sign)) {
                    tag = "zzscpaidui";
                } else {
                    return MessageResult.error("签名无效");
                }
            }
        }

        EntityWrapper<AsyncNotificationBusiness> asyncNotificationBusinessEntityWrapper = new EntityWrapper<AsyncNotificationBusiness>();
        asyncNotificationBusinessEntityWrapper.eq("order_id", orderId).eq("tag", tag).eq("type", type).orderBy("create_time", false);
        AsyncNotificationBusiness asyncNotificationBusiness = iAsyncNotificationBusinessService.selectOne(asyncNotificationBusinessEntityWrapper);
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("orderId", asyncNotificationBusiness.getOrderId());
        stringStringMap.put("traderSn", asyncNotificationBusiness.getTraderSn());
        stringStringMap.put("status", asyncNotificationBusiness.getStatus());
        String signBody = null;

        switch (tag) {
            case "zzsc":
                signBody = MD5Util.md5Encode(asyncNotificationBusiness.getTraderSn()
                        + "zzsc2019"
                ).toLowerCase();
                break;
            case "etgsc":
                signBody = MD5Util.md5Encode(asyncNotificationBusiness.getTraderSn()
                        + "EtgSc2019"
                ).toLowerCase();
                break;
            case "zzscpaidui":
                signBody = MD5Util.md5Encode(asyncNotificationBusiness.getTraderSn()
                        + "paid2019"
                ).toLowerCase();
                break;
            default:
                log.info("无法进行sign {}", tag);
                break;
        }

        stringStringMap.put("sign", signBody);
        return MessageResult.success("ok", stringStringMap);

    }


    private MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, String toAddress, String tradeUnit, BigDecimal amount, HttpServletRequest request,
                                                              PayTransferType transferType, String platform, String platformTo, String tag) {


        Member fromMember = memberService.findOne(fromMemberId);
        memberService.checkRealName(fromMember);
        //验证资金密码
        String jyPassword = request.getHeader("cmd");
        if (jyPassword == null || "".equals(jyPassword)) {
            return error("请输入资金密码");
        }
        String jyPass = new SimpleHash("md5", jyPassword, fromMember.getSalt(), 2).toHex().toLowerCase();
        String mbPassword = fromMember.getJyPassword();
        if (mbPassword == null || "".equals(mbPassword)) {
            return error("请设置资金密码");
        }
        if (!StringUtils.equalsIgnoreCase(mbPassword, jyPass)) {
            return error("资金密码错误");
        }

        return platformTransfer(fromMemberId, toAddress, tradeUnit, amount, transferType, platform, platformTo, tag);
    }

    /**
     * 平台内互转（无登录拦截）
     *
     * @param fromMemberId 支付用户id
     * @param toAddress    收款用户地址
     * @param tradeUnit    支付币种
     * @param amount       支付数额
     * @param transferType 交易类型
     * @param platform     转账方应用ID
     * @param platformTo   收款方应用ID
     * @author Zhang Yanjun
     * @time 2019.01.16 17:52
     */
    private MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, String toAddress, String tradeUnit, BigDecimal amount,
                                                              PayTransferType transferType, String platform, String platformTo, String tag) {
        if (CommonUtils.isEmpty(fromMemberId) || CommonUtils.isEmpty(toAddress) || CommonUtils.isEmpty(tradeUnit) || CommonUtils.isEmpty(amount)) {
            return error("缺少必填参数");
        }
        Member fromMember = memberService.findOne(fromMemberId);

        //转入账户
        MemberWallet receive = memberWalletService.findCacheByCoinUnitAndAddress(tradeUnit, toAddress);
        if (receive == null) {
            return error("缺少转入账户");
        }


        Member toMember = memberService.findOne(receive.getMemberId());
        if (fromMember == null || toMember == null) {
            return error("转入用户不存在");
        }


        //验证用户是否被禁止交易
        if (fromMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)
                || toMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            return error("禁止交易");
        }
        validateOpenTranscationService.validateOpenPlatformTransaction(fromMemberId, sourceService.getMessage("ACCOUNT_DISABLE"));
        validateOpenTranscationService.validateOpenPlatformTransaction(toMember.getId(), sourceService.getMessage("ACCOUNT_DISABLE"));
        //转出账户
        MemberWallet forward = memberWalletService.findByCoinUnitAndMemberId(tradeUnit, fromMemberId);
        if (forward == null) {
            return error("缺少转出账户");
        }


        //扫码支付时，验证收款账号是否为商家角色
        if (transferType == PayTransferType.PAYMENT_CODE) {
            PayWalletPlatMemberBind platMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(receive.getMemberId(), platformTo);
            if (platMemberBind == null || platMemberBind.getRoleId() == 1L) {
                return error("接收方不是商家");
            }
        }

        if (StringUtils.equalsIgnoreCase(fromMember.getId().toString(), toMember.getId().toString())) {
            return error("接收方不能和发送方相同");
        }

        MemberSecuritySet fromSecurity = memberSecuritySetService.findOneBymemberId(fromMember.getId());
        MemberSecuritySet toSecurity = memberSecuritySetService.findOneBymemberId(toMember.getId());

        if (fromSecurity != null && BooleanEnum.IS_FALSE.equals(fromSecurity.getIsOpenPlatformTransaction())) {
            return error("发送方禁止转账");
        }

        if (toSecurity != null && BooleanEnum.IS_FALSE.equals(toSecurity.getIsOpenPlatformTransaction())) {
            return error("接收方禁止转账");
        }

        log.info("钱包支付，平台互转开始==fromMemberId-{}==toMemberId-{}=======", fromMemberId, receive.getMemberId());
        MessageRespResult result;
        try {
            result = payWalletService.platformTransfer(forward, receive, amount, fromMember,
                    toMember, restTemplate, transferType, platform, platformTo, tag);
        } catch (Exception e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
        log.info("钱包支付，平台互转结束==fromMemberId-{}==toMemberId-{}===result-{}====", fromMemberId, receive.getMemberId(), result);
        return result;
    }
}
