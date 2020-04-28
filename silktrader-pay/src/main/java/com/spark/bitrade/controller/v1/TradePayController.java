package com.spark.bitrade.controller.v1;

import com.spark.bitrade.annotation.definition.ApiRequestLimit;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.dto.WalletQRPayParameterDTO;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.exception.ApiException;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.CommonUtils;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import static com.spark.bitrade.util.MessageRespResult.error;


/**
 * <p>钱包扫码支付接口</p>
 *
 * @author tian.bo
 * @date 2019/1/8.
 */
@RestController
@RequestMapping("/QRCode/api")
@Slf4j
public class TradePayController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private IWalletQRPayService walletQRPayService;

	@Autowired
	private PayWalletNewService payWalletService;
	@Autowired
	private MemberWalletService memberWalletService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private IPayWalletPlatMemberBindService iPayWalletPlatMemberBindService;

	@Autowired
	private ValidateOpenTranscationService validateOpenTranscationService;
	@Autowired
	private LocaleMessageSourceService sourceService;

	@Autowired
	private MemberSecuritySetService memberSecuritySetService;

	@Value("${transfer.unable:true}")
	private Boolean transferUnable;
	/**
	 * 扫码支付
	 *
	 * @param dto     WalletQRPayParameterDTO
	 * @param request
	 * @return
	 */
	@ApiRequestLimit(count = 10000)
	//@Decrypt(decryptType = EncryptType.AES)
	@PostMapping("/gateway")
	public MessageResult gateway(@RequestBody WalletQRPayParameterDTO dto, HttpServletRequest request) {
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
		log.info(" trans {}", dto);
		if(transferUnable) {
			// TODO 业务暂时调整，禁止该业务
			return MessageResult.error("业务暂时无法使用");
		}
		/**
		 * 2.远程转账服务
		 */

		MessageRespResult<PayFastRecord> messageResResult = platformTransfer (Long.valueOf(dto.getFrom())
				, Long.valueOf(dto.getTo())
				, dto.getSymbol()
				, dto.getAmount()
				, request
				, PayTransferType.PAY_FAST
				, dto.getFromAppId()
				, dto.getToAppId()
		);

		MessageResult messageResult = new MessageResult(messageResResult.getCode(),messageResResult.getMessage(),messageResResult.getData());
		log.info(" trans {}", messageResult);
		return messageResult;

	}


	private MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, Long toMemberId, String tradeUnit, BigDecimal amount, HttpServletRequest request,
															  PayTransferType transferType, String platform, String platformTo) {


		Member fromMember = memberService.findOne(fromMemberId);
		memberService.checkRealName(fromMember);
		//验证资金密码
		String jyPassword = request.getHeader("cmd");
		if (jyPassword == null || "".equals(jyPassword)) {
			return error(MessageCode.MISSING_JYPASSWORD);
		}
		String jyPass = new SimpleHash("md5", jyPassword, fromMember.getSalt(), 2).toHex().toLowerCase();
		String mbPassword = fromMember.getJyPassword();
		if (mbPassword == null || "".equals(mbPassword)) {
			return error(MessageCode.NO_SET_JYPASSWORD);
		}
		if (!StringUtils.equalsIgnoreCase(mbPassword,jyPass)) {
			return error(MessageCode.ERROR_JYPASSWORD);
		}

		return platformTransfer(fromMemberId, toMemberId, tradeUnit, amount, transferType, platform, platformTo);
	}

	/**
	 * 平台内互转（无登录拦截）
	 *
	 * @param fromMemberId 支付用户id
	 * @param toMemberId   收款用户id
	 * @param tradeUnit    支付币种
	 * @param amount       支付数额
	 * @param transferType 交易类型
	 * @param platform     转账方应用ID
	 * @param platformTo   收款方应用ID
	 * @author Zhang Yanjun
	 * @time 2019.01.16 17:52
	 */
	private MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, Long toMemberId, String tradeUnit, BigDecimal amount,
															  PayTransferType transferType, String platform, String platformTo) {
		if (CommonUtils.isEmpty(fromMemberId) || CommonUtils.isEmpty(toMemberId) || CommonUtils.isEmpty(tradeUnit) || CommonUtils.isEmpty(amount)) {
			return error(MessageCode.INVALID_PARAMETER);
		}
		Member fromMember = memberService.findOne(fromMemberId);
		Member toMember = memberService.findOne(toMemberId);
		if (fromMember == null || toMember == null) {
			return error(MessageCode.MISSING_USER);
		}


		//验证用户是否被禁止交易
		if (fromMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)
				|| toMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
			return error(MessageCode.ACCOUNT_DISABLE);
		}

		MemberSecuritySet fromSecurity = memberSecuritySetService.findOneBymemberId(fromMember.getId());
		MemberSecuritySet toSecurity = memberSecuritySetService.findOneBymemberId(toMember.getId());

		if(fromSecurity != null && BooleanEnum.IS_FALSE.equals(fromSecurity.getIsOpenPlatformTransaction())){
			return error("发送方禁止转账");
		}

		if(toSecurity != null && BooleanEnum.IS_FALSE.equals(toSecurity.getIsOpenPlatformTransaction())){
			return error("接收方禁止转账");
		}
		validateOpenTranscationService.validateOpenPlatformTransaction(fromMemberId,sourceService.getMessage("ACCOUNT_DISABLE"));
		validateOpenTranscationService.validateOpenPlatformTransaction(toMemberId,sourceService.getMessage("ACCOUNT_DISABLE"));
		//转出账户
		MemberWallet forward = memberWalletService.findByCoinUnitAndMemberId(tradeUnit, fromMemberId);
		if (forward == null) {
			return error(MessageCode.MISSING_ACCOUNT);
		}
		//转入账户
		MemberWallet receive = memberWalletService.findByCoinUnitAndMemberId(tradeUnit, toMemberId);
		if (receive == null) {
			return error(MessageCode.MISSING_ACCOUNT);
		}

		//扫码支付时，验证收款账号是否为商家角色
		if (transferType == PayTransferType.PAYMENT_CODE) {
			PayWalletPlatMemberBind platMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(toMemberId, platformTo);
			if (platMemberBind == null || platMemberBind.getRoleId() == 1L) {
				return error(MessageCode.RECEIVER_NEED_MERCHANT);
			}
		}

		log.info("钱包支付，平台互转开始==fromMemberId-{}==toMemberId-{}=======", fromMemberId, toMemberId);
		MessageRespResult result;
		try {
			result = payWalletService.platformTransfer(forward, receive, amount, fromMember,
					toMember, restTemplate, transferType, platform, platformTo);
		} catch (Exception e) {
			e.printStackTrace();
			return error(MessageCode.convertToMessageCode(e.getMessage()));
		}
		log.info("钱包支付，平台互转结束==fromMemberId-{}==toMemberId-{}===result-{}====", fromMemberId, toMemberId, result);
		return result;
	}
}
