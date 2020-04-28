package com.spark.bitrade.controller.v1;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.constant.BooleanEnum;
import com.spark.bitrade.constant.CommonStatus;
import com.spark.bitrade.constant.PayTransferType;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.dto.PayAccountVo;
import com.spark.bitrade.entity.*;
import com.spark.bitrade.entity.transform.AuthMember;
import com.spark.bitrade.enums.MessageCode;
import com.spark.bitrade.service.*;
import com.spark.bitrade.util.CommonUtils;
import com.spark.bitrade.util.MessageRespResult;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vo.PayCoinVo;
import com.spark.bitrade.vo.PayToMemberVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spark.bitrade.constant.SysConstant.SESSION_MEMBER;
import static com.spark.bitrade.util.MessageRespResult.error;
import static com.spark.bitrade.util.MessageRespResult.success;
import static org.springframework.util.Assert.isTrue;

/**
 * 账户控制器
 *
 * @author tansitao
 * @time 2019.01.09 15:29
 */
@Api(description = "账户控制器")
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    @Autowired
    private IPaySupportCoinConfigService iPaySupportCoinConfigService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private PayWalletNewService payWalletService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private IPayFastRecordService iPayFastRecordService;
    @Autowired
    private IPayWalletMemberBindService iPayWalletMemberBindService;
    @Autowired
    private IPayRoleFeeRateConfigService iPayRoleFeeRateConfigService;
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
     * 查询账户列表（支持币种）
     * @author Zhang Yanjun
     * @time 2019.01.10 16:47
     * @param member
     */
    @ApiOperation(value = "查询账户列表（支持币种）")
    @PostMapping("list")
    public MessageRespResult<List<PayAccountVo>> accountList(@SessionAttribute(SESSION_MEMBER) AuthMember member){
        log.info("查询钱包支付（已支持币种）账户列表，memberId-{}=======",member.getId());
        List<PayAccountVo> list = iPaySupportCoinConfigService.findAccountByValidCoinAndAppIdOrderByRankDesc(member.getId(), member.getPlatform());
        return success("查询成功",list);
    }


    /**
     * 支付币种列表
     * @author Zhang Yanjun
     * @time 2019.01.11 9:27
     * @param
     */
    @ApiOperation(value = "支付币种列表")
    @PostMapping("supportCoinByAppId")
    public  MessageRespResult supportCoin(String appId){
        List<PaySupportCoinConfig> listSupport = iPaySupportCoinConfigService.findAllByStatusAndAppIdOrderByRankDesc(
                BooleanEnum.IS_TRUE, appId);

        List<PayCoinVo> listCoin = new ArrayList<>();
        for (PaySupportCoinConfig paySupportCoinConfig: listSupport){
            Coin coin = coinService.findByUnit(paySupportCoinConfig.getUnit());
            if (coin.getStatus().equals(CommonStatus.NORMAL)){
                PayCoinVo payCoinVo = new PayCoinVo();
                payCoinVo.setUnit(coin.getUnit());
                payCoinVo.setStatus(coin.getStatus());
                payCoinVo.setMinTxFee(coin.getMinTxFee());
                payCoinVo.setCnyRate(coin.getCnyRate());
                payCoinVo.setMaxTxFee(coin.getMaxTxFee());
                payCoinVo.setEnableRpc(coin.getEnableRpc());
                payCoinVo.setBaseCoinUnit(coin.getBaseCoinUnit());
                payCoinVo.setCanWithdraw(coin.getCanWithdraw());
                payCoinVo.setCanRecharge(coin.getCanRecharge());
                payCoinVo.setCanTransfer(coin.getCanTransfer());
                payCoinVo.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                payCoinVo.setWithdrawThreshold(coin.getWithdrawThreshold());
                payCoinVo.setMinWithdrawAmount(coin.getMinWithdrawAmount());
                payCoinVo.setMaxWithdrawAmount(coin.getMaxWithdrawAmount());
                payCoinVo.setWithdrawScale(coin.getWithdrawScale());
                payCoinVo.setFeeRate(coin.getFeeRate());
                payCoinVo.setFeeType(coin.getFeeType());
                payCoinVo.setHasLabel(coin.getHasLabel());
                payCoinVo.setMinDepositAmount(coin.getMinDepositAmount());
                payCoinVo.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                payCoinVo.setFeeDiscountType(coin.getFeeDiscountType());
                payCoinVo.setFeeDiscountAmount(coin.getFeeDiscountAmount());

                listCoin.add(payCoinVo);
            }
        }

        Map<String,Object> mapCoin = new HashMap<>();
        mapCoin.put("support",listSupport);
        mapCoin.put("coin",listCoin);
        return success("查询成功", mapCoin);
    }



    /**
     * 无条件获取支付币种
     * @author shenzucai
     * @time 2019.03.23 18:37
     * @return true
     */
    @ApiOperation(value = "无条件获取支付币种列表")
    @PostMapping("supportCoinNoCondition")
    public  MessageRespResult supportCoinNoCondition(){

        List<Coin> coins = coinService.findAll();
        List<PayCoinVo> listCoin = new ArrayList<>();
        for (Coin coin: coins){
            if (coin.getStatus().equals(CommonStatus.NORMAL)){
                PayCoinVo payCoinVo = new PayCoinVo();
                payCoinVo.setUnit(coin.getUnit());
                payCoinVo.setStatus(coin.getStatus());
                payCoinVo.setMinTxFee(coin.getMinTxFee());
                payCoinVo.setCnyRate(coin.getCnyRate());
                payCoinVo.setMaxTxFee(coin.getMaxTxFee());
                payCoinVo.setEnableRpc(coin.getEnableRpc());
                payCoinVo.setBaseCoinUnit(coin.getBaseCoinUnit());
                payCoinVo.setCanWithdraw(coin.getCanWithdraw());
                payCoinVo.setCanRecharge(coin.getCanRecharge());
                payCoinVo.setCanTransfer(coin.getCanTransfer());
                payCoinVo.setCanAutoWithdraw(coin.getCanAutoWithdraw());
                payCoinVo.setWithdrawThreshold(coin.getWithdrawThreshold());
                payCoinVo.setMinWithdrawAmount(coin.getMinWithdrawAmount());
                payCoinVo.setMaxWithdrawAmount(coin.getMaxWithdrawAmount());
                payCoinVo.setWithdrawScale(coin.getWithdrawScale());
                payCoinVo.setFeeRate(coin.getFeeRate());
                payCoinVo.setFeeType(coin.getFeeType());
                payCoinVo.setHasLabel(coin.getHasLabel());
                payCoinVo.setMinDepositAmount(coin.getMinDepositAmount());
                payCoinVo.setFeeDiscountCoinUnit(coin.getFeeDiscountCoinUnit());
                payCoinVo.setFeeDiscountType(coin.getFeeDiscountType());
                payCoinVo.setFeeDiscountAmount(coin.getFeeDiscountAmount());
                listCoin.add(payCoinVo);
            }
        }
        Map<String,Object> mapCoin = new HashMap<>();
        mapCoin.put("coin",listCoin);
        return success("查询成功", mapCoin);
    }

    /**
     * 支付币种列表
     * @author Zhang Yanjun
     * @time 2019.01.11 9:27
     * @param
     */
    @ApiOperation(value = "支付币种列表")
    @PostMapping("supportCoin")
    public  MessageRespResult supportCoin(@SessionAttribute(SESSION_MEMBER) AuthMember member){
        return supportCoin(member.getPlatform());
    }

    /**
     * 支付币种列表
     * @author Zhang Yanjun
     * @time 2019.01.11 9:27
     * @param
     */
    @ApiOperation(value = "支付币种列表")
    @PostMapping("supportCoinByReq")
    public  MessageRespResult supportCoin(HttpServletRequest request){
        String appId = request.getHeader("thirdMark");
        return supportCoin(appId);
    }


    @ApiOperation(value = "平台内互转")
    @PostMapping("platformTransfer")
    public MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, Long toMemberId, String tradeUnit, BigDecimal amount, HttpServletRequest request,
                                                             PayTransferType transferType, String platform, String platformTo){
        //验证资金密码
//        String jyPassword = request.getHeader("cmd");
//        if (jyPassword == null || "".equals(jyPassword)) {
//            return error(MessageCode.MISSING_JYPASSWORD);
//        }
//        String jyPass = new SimpleHash("md5", jyPassword, fromMember.getSalt(), 2).toHex().toLowerCase();
//        String mbPassword = fromMember.getJyPassword();
//        if (mbPassword == null || "".equals(mbPassword)) {
//            return error(MessageCode.NO_SET_JYPASSWORD);
//        }
//        if (jyPass.equals(mbPassword)) {
//            return error(MessageCode.ERROR_JYPASSWORD);
//        }

        return platformTransfer(fromMemberId, toMemberId, tradeUnit, amount, transferType, platform, platformTo);
    }

    /**
     * 平台内互转（无登录拦截）
     * @author Zhang Yanjun
     * @time 2019.01.16 17:52
     * @param fromMemberId 支付用户id
     * @param toMemberId 收款用户id
     * @param tradeUnit 支付币种
     * @param amount 支付数额
     * @param transferType 交易类型
     * @param platform 转账方应用ID
     * @param platformTo 收款方应用ID
     *
     */
    @ApiOperation(value = "扫码枪平台内互转")
    @PostMapping("platformTransferByMemberId")
    public MessageRespResult<PayFastRecord> platformTransfer(Long fromMemberId, Long toMemberId, String tradeUnit, BigDecimal amount,

                                                             PayTransferType transferType, String platform, String platformTo) {

        if(transferUnable) {
            // TODO 业务暂时调整，禁止该业务
            return MessageRespResult.error("业务暂时无法使用");
        }
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
            return error(MessageCode.ACCOUNT_DISABLE);
        }

        if(toSecurity != null && BooleanEnum.IS_FALSE.equals(toSecurity.getIsOpenPlatformTransaction())){
            return error(MessageCode.ACCOUNT_DISABLE);
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
        if (transferType == PayTransferType.PAYMENT_CODE){
            PayWalletPlatMemberBind platMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(toMemberId,platformTo);
            if (platMemberBind == null || platMemberBind.getRoleId() == 1L){
                return error(MessageCode.RECEIVER_NEED_MERCHANT);
            }
        }

        log.info("钱包支付，平台互转开始==fromMemberId-{}==toMemberId-{}=======", fromMemberId, toMemberId);
        MessageRespResult result;
        try {
            result = payWalletService.platformTransfer(forward, receive, amount, fromMember,
                    toMember, restTemplate,transferType, platform, platformTo);
        } catch (Exception e) {
//            return error(MessageCode.ERROR);
            return error(MessageCode.convertToMessageCode(e.getMessage()));
        }
        log.info("钱包支付，平台互转结束==fromMemberId-{}==toMemberId-{}===result-{}====", fromMemberId, toMemberId, result);
        return result;
    }

    /**
     * 平台内通过手机号互转（无登录拦截）
     * @author yangch
     * @time 2019-02-21 09:39:19
     * @param fromPhone 支付用户手机号
     * @param toPhone 收款用户手机号
     * @param tradeUnit 支付币种
     * @param amount 支付数额
     * @param transferType 交易类型
     * @param platform 转账方应用ID
     * @param platformTo 收款方应用ID
     */
    @ApiOperation(value = "平台内通过手机号互转")
    @PostMapping("platformTransferByPhone")
    public MessageRespResult<PayFastRecord> platformTransferByPhone(String fromPhone, String toPhone, String tradeUnit, BigDecimal amount,
                                                                    PayTransferType transferType, String platform, String platformTo){

        if(transferUnable) {
            // TODO 业务暂时调整，禁止该业务
            return MessageRespResult.error("业务暂时无法使用");
        }
        if (CommonUtils.isEmpty(fromPhone)||CommonUtils.isEmpty(toPhone)
                || CommonUtils.isEmpty(tradeUnit) || CommonUtils.isEmpty(amount)) {
            return error(MessageCode.INVALID_PARAMETER);
        }
        Member fromMember = memberService.findByPhone(fromPhone);
        Member toMember = memberService.findByPhone(toPhone);
        if (fromMember == null|| toMember == null){
            return error(MessageCode.MISSING_USER);
        }

        //验证用户是否被禁止交易
        if (fromMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)
                || toMember.getTransactionStatus().equals(BooleanEnum.IS_FALSE)){
            return error(MessageCode.ACCOUNT_DISABLE);
        }

        MemberSecuritySet fromSecurity = memberSecuritySetService.findOneBymemberId(fromMember.getId());
        MemberSecuritySet toSecurity = memberSecuritySetService.findOneBymemberId(toMember.getId());

        if(fromSecurity != null && BooleanEnum.IS_FALSE.equals(fromSecurity.getIsOpenPlatformTransaction())){
            return error(MessageCode.ACCOUNT_DISABLE);
        }

        if(toSecurity != null && BooleanEnum.IS_FALSE.equals(toSecurity.getIsOpenPlatformTransaction())){
            return error(MessageCode.ACCOUNT_DISABLE);
        }

        validateOpenTranscationService.validateOpenPlatformTransaction(fromMember.getId(),sourceService.getMessage("ACCOUNT_DISABLE"));
        validateOpenTranscationService.validateOpenPlatformTransaction(toMember.getId(),sourceService.getMessage("ACCOUNT_DISABLE"));
        //转出账户
        MemberWallet forward = memberWalletService.findByCoinUnitAndMemberId(tradeUnit, fromMember.getId());
        if (forward == null){
            return error(MessageCode.MISSING_ACCOUNT);
        }
        //转入账户
        MemberWallet receive =  memberWalletService.findByCoinUnitAndMemberId(tradeUnit, toMember.getId());
        if (receive == null){
            Coin coin = coinService.findByUnit(tradeUnit);
            receive = memberWalletService.createMemberWallet(toMember.getId(), coin);
        }
        log.info("钱包支付，平台互转开始==fromMemberId-{}==toMemberId-{}=======", fromMember.getId(), toMember.getId());
        log.info("参数：fromPhone={},  toPhone={},  tradeUnit={},  amount={}, transferType={},  platform={},  platformTo={}",
                fromPhone,  toPhone,  tradeUnit,  amount, transferType,  platform,  platformTo);

        MessageRespResult result;
        try {
            if (transferType == PayTransferType.EXCHANGE) {

                result = payWalletService.platformExchange(forward, receive, amount, fromMember, toMember,
                        restTemplate, transferType, platform, platformTo);
            } else {
                result = payWalletService.platformTransfer(forward, receive, amount, fromMember, toMember,
                        restTemplate, transferType, platform, platformTo);
            }
        } catch (Exception e) {
            log.error("平台互转失败", e);
//            return error(MessageCode.ERROR);
            return error(MessageCode.convertToMessageCode(e.getMessage()));
        }
        log.info("钱包支付，平台互转结束==fromMemberId-{}==toMemberId-{}===result-{}====", fromMember.getId(), toMember.getId(), result);
        return result;
    }


    /**
      * 获取用户新币的地址
      * @author tansitao
      * @time 2019/1/18 16:39 
      */
    @RequestMapping("wallet/getCoinAddr")
    @Transactional(rollbackFor = Exception.class)
    public MessageRespResult findWalletAddrByCoin(@SessionAttribute(SESSION_MEMBER) AuthMember member, String unit) {
        Member m = memberService.findOne(member.getId());
        return findWalletAddr(m,unit);
    }

    /**
      * 获取用户新币的地址
      * @author tansitao
      * @time 2019/1/18 16:39 
      */
    @RequestMapping("wallet/getCoinAddrByUsername")
    @Transactional(rollbackFor = Exception.class)
    public MessageRespResult findWalletAddrByCoin(String username, String unit) {
        Member m = memberService.findMemberByMobilePhoneOrEmail(username, username);
        if (m == null) {
            return error(MessageCode.MISSING_USER);
        }
        return findWalletAddr(m, unit);
    }

    /**
      * 获取用户新币的地址
      * @author tansitao
      * @time 2019/1/18 16:39 
      */
    @RequestMapping("wallet/getCoinAddrByMember")
    @Transactional(rollbackFor = Exception.class)
    public MessageRespResult findWalletAddr(Member member, String unit) {
        Coin coin = coinService.findByUnit(unit);
        isTrue(coin != null, unit + " " + msService.getMessage("COIN_NOT_EXIST"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
        Member userMember = new Member();
        userMember.setId(member.getId());
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-RPC-" + coin.getUnit();
        String account = "U" + member.getId();
//        coin.setName(coinName);
        MemberWallet wallet = memberWalletService.findByCoinAndMember(coin, userMember);

        //add|edit|del by  shenzucai 时间： 2019.02.19  原因：由于后面返回
        MemberWallet memberWallet = new MemberWallet();
        //如果该币已有钱包地址返回结果
        if (wallet != null && wallet.getAddress() != null && !"".equals(wallet.getAddress())) {
//            return new MessageResult(100, unit + msService.getMessage("ADDR_HAS_EXIST"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
//            return error(MessageCode.ADDR_HAS_EXIST);
            return success("success",wallet);
        }
        //如果该币无钱包信息或地址，先获取该币钱包地址
        else {
            String address = "";
            try {
                //edit by shenzucai 时间： 2018.04.22 原因：判断是否为代币，如果不是，则走rpc获取地址 start--------------
                if (StringUtils.isEmpty(coin.getBaseCoinUnit())) {
                    String url = "http://" + serviceName + "/rpc/address/{account}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                    log.info("remote call:service={},result={}", serviceName, result);
                    if (result.getStatusCode().value() == 200) {
                        MessageResult mr = result.getBody();
                        log.info("mr={}", mr);
                        if (mr.getCode() == 0) {
                            //返回地址成功，调用持久化
                            address = (String) mr.getData();
                            if(StringUtils.isEmpty(address)) {
//                                return new MessageResult(500, msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
//                                return error(MessageCode.WALLET_GET_FAIL);
                                return error(500,msService.getMessage("WALLET_GET_FAIL"));
                            }

                        }else{
//                            return new MessageResult(500, unit + msService.getMessage("WALLET_GET_FAIL"));//add by tansitao 时间： 2018/7/25 原因：钱包接口返回异常获取钱包失败
//                            return error(MessageCode.WALLET_GET_FAIL);
                            return error(500,unit+ " " + msService.getMessage("WALLET_GET_FAIL"));
                        }
                    }else{
//                        return new MessageResult(500, unit + msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
//                        return error(MessageCode.WALLET_GET_FAIL);
                        return error(500,unit + " " + msService.getMessage("WALLET_GET_FAIL"));
                    }
                } else {
                    //add|edit|del by  shenzucai 时间： 2018.06.21  原因：如果获取的是代币的地址，且主币地址也不存在的时候，就会生成主币和代币的地址
                    Coin baseCoin = coinService.findByUnit(coin.getBaseCoinUnit());
                    MemberWallet baseWallet = memberWalletService.findByCoinAndMember(baseCoin, userMember);
                    //edit by tansitao 时间： 2018/5/21 原因：修改对钱包为null的判断
                    if(baseWallet == null || StringUtils.isEmpty(baseWallet.getAddress())){

                        //远程RPC服务URL,后缀为币种单位
                        String tempName = "SERVICE-RPC-" + coin.getBaseCoinUnit();
                        String url = "http://" + tempName + "/rpc/address/{account}";
                        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, account);
                        String baseAddress = null;
                        log.info("remote call:service={},result={}", serviceName, result);
                        if (result.getStatusCode().value() == 200) {

                            MessageResult mr = result.getBody();
                            log.info("mr={}", mr);
                            if (mr.getCode() == 0) {
                                //返回地址成功，调用持久化
                                baseAddress = (String) mr.getData();
                            }
                        }

                        if(!StringUtils.isEmpty(baseAddress)) {
                            if(baseWallet == null) {
                                MemberWallet baseWallet1 = new MemberWallet();
                                baseWallet1.setAddress(baseAddress);
                                baseWallet1.setBalance(BigDecimal.valueOf(0));
                                baseWallet1.setCoin(baseCoin);
                                baseWallet1.setFrozenBalance(BigDecimal.valueOf(0));
                                baseWallet1.setLockBalance(BigDecimal.valueOf(0));
                                baseWallet1.setMemberId(member.getId());
                                baseWallet1.setIsLock(BooleanEnum.IS_FALSE);
                                memberWalletService.save(baseWallet1);
                            }else{
                                //add by  shenzucai 时间： 2018.11.20  原因：由于原先操作可能影响账，遂改成sql形式
                                // baseWallet.setAddress(baseAddress);
                                // walletService.save(baseWallet);
                                memberWalletService.updateMemberWalletAddress(baseWallet.getId(),baseAddress);
                            }
                            address = baseAddress;
                        }else{
//                            return new MessageResult(500, msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
//                            return error(MessageCode.WALLET_GET_FAIL);
                            return error(500,msService.getMessage("WALLET_GET_FAIL"));
                        }


                        // logger.error("请先获取{}地址，然后再获取{}地址", baseCoin.getName(), coinName);

                    }else {
                        address = baseWallet.getAddress();
                    }
                }
                //edit by shenzucai 时间： 2018.04.22 原因：判断是否为代币，如果不是，则走rpc获取地址 end -----------------------
            } catch (Exception e) {
                log.error("call {} failed,error={}", serviceName, e.getMessage());
//                return new MessageResult(500, unit + msService.getMessage("WALLET_GET_FAIL"));//edit by tansitao 时间： 2018/5/21 原因：修改为国际化
//                return error(MessageCode.WALLET_GET_FAIL);
                return error(500,unit + " " + msService.getMessage("WALLET_GET_FAIL"));
            }
            //如果该用户无钱包信息，创建新的钱包信息，并添加其他基本信息
            if (null == wallet) {
                wallet = new MemberWallet();
                wallet.setAddress(address);
                wallet.setBalance(BigDecimal.valueOf(0));
                wallet.setCoin(coin);
                wallet.setFrozenBalance(BigDecimal.valueOf(0));
                wallet.setLockBalance(BigDecimal.valueOf(0));
                wallet.setMemberId(member.getId());
                wallet.setIsLock(BooleanEnum.IS_FALSE);
                memberWalletService.save(wallet);
            }
            //如果有钱包信息，则设置钱包地址
            else {
                //add by  shenzucai 时间： 2018.11.20  原因：由于原先操作可能影响账，遂改成sql形式
                // wallet.setAddress(address);
                memberWalletService.updateMemberWalletAddress(wallet.getId(),address);
            }

            BeanUtils.copyProperties(wallet,memberWallet);
            memberWallet.setAddress(address);
        }
        MessageRespResult mr = success("success");
        mr.setData(memberWallet);
        return mr;
    }



    @ApiOperation(value = "快速转账/收款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种(为空查全部)",name = "unit"),
            @ApiImplicitParam(value = "转账类型（0转入 1转出）",name = "tradeType",required = true),
            @ApiImplicitParam(value = "交易类型",name = "transferType",required = true),
            @ApiImplicitParam(value = "开始时间",name = "startTime"),
            @ApiImplicitParam(value = "结束时间",name = "endTime"),
            @ApiImplicitParam(value = "页码",name = "pageNo",required = true),
            @ApiImplicitParam(value = "页大小",name = "pageSize",required = true)
    })
    @PostMapping("fastRecord")
    public MessageRespResult<PageData<PayFastRecord>> fastRecord(String unit, int pageNo, int pageSize, int tradeType,PayTransferType transferType,
                                                                 @SessionAttribute(SESSION_MEMBER) AuthMember member,String startTime,String endTime){
        if (transferType == null || "".equals(transferType)){
            return error(MessageCode.REQUIRED_PARAMETER);
        }
        PageInfo<PayFastRecord> pageInfo = iPayFastRecordService.findByMember(unit,member.getId(),pageNo,pageSize,
                tradeType,transferType,member.getPlatform(),member.getPlatform(),startTime,endTime);
        return success("查询成功", PageData.toPageData(pageInfo));
    }

    //todo 移至recordController
//    @ApiOperation(value = "云端流水记录")
//    @ApiImplicitParams({
//            @ApiImplicitParam(value = "交易类型(1云端支付-（云端钱包<->云端钱包），2兑换，3支付码支付) 为空查全部", name = "transferType"),
//            @ApiImplicitParam(value = "开始时间", name = "startTime"),
//            @ApiImplicitParam(value = "结束时间", name = "endTime"),
//            @ApiImplicitParam(value = "页码", name = "pageNo", required = true),
//            @ApiImplicitParam(value = "页大小", name = "pageSize", required = true),
//            @ApiImplicitParam(value = "转账方用户id", name = "fromId"),
//            @ApiImplicitParam(value = "转账方手机号（模糊查询）", name = "fromPhone"),
//            @ApiImplicitParam(value = "转账方应用id", name = "fromAppid", required = true),
//            @ApiImplicitParam(value = "收款方用户id", name = "toId"),
//            @ApiImplicitParam(value = "收款方手机号（模糊查询）", name = "toPhone"),
//            @ApiImplicitParam(value = "收款方应用id", name = "toAppid", required = true),
//    })
//    @PostMapping("cloudRecord")
//    public MessageRespResult<PageData<PayFastRecord>> cloudRecord(int pageNo, int pageSize, PayTransferType transferType, String startTime, String endTime,
//                                                                  Long fromId, Long toId, String fromPhone, String toPhone, String fromAppid, String toAppid) {
//        PageInfo<PayFastRecord> pageInfo = iPayFastRecordService.findlist(pageNo, pageSize, transferType, startTime, endTime,
//                fromId, toId, fromPhone, toPhone, fromAppid, toAppid);
//        return success("查询成功", PageData.toPageData(pageInfo));
//    }

    //todo 移至recordController，弃用后删除
//    @ApiOperation(value = "云端流水记录")
//    @ApiImplicitParams({
//            @ApiImplicitParam(value = "币种", name = "unit"),
//            @ApiImplicitParam(value = "开始时间", name = "startTime"),
//            @ApiImplicitParam(value = "结束时间", name = "endTime"),
//            @ApiImplicitParam(value = "页码", name = "pageNo", required = true),
//            @ApiImplicitParam(value = "页大小", name = "pageSize", required = true)
//    })
//    @PostMapping("getFastRecord")
//    public MessageRespResult<PageData<PayRecordDto>> getFastRecord(@SessionAttribute(SESSION_MEMBER) AuthMember user,String unit,int pageNo, int pageSize,
//                                                                   String startTime, String endTime) {
//        PageInfo<PayFastRecord> pageInfo = iPayFastRecordService.findFastRecord(unit,pageNo, pageSize, user.getId(), user.getPlatform(),startTime, endTime);
//        List<PayFastRecord> fastRecordList = pageInfo.getList();
//        List<PayRecordDto> payRecordDtoList = iPayFastRecordService.getFastRecord(user.getId(), user.getPlatform(),fastRecordList);
//        return success4Data(PageData.toPageData(pageInfo,payRecordDtoList));
//    }


    @ApiOperation(value = "查询收款方信息")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "币种",name = "unit"),
            @ApiImplicitParam(value = "地址",name = "address")
    })
    @PostMapping("toMemberInfo")
    public MessageRespResult<PayToMemberVo> toMemberInfo(String unit,String address,HttpServletRequest request){
        String thirdMark = request.getHeader("thirdMark");
        PayToMemberVo payToMemberVo = new PayToMemberVo();
        MemberWallet memberWallet;
        long memberId = 0;
        payToMemberVo.setIsAddress(BooleanEnum.IS_FALSE);


        if(StringUtils.isEmpty(unit)){
            return error(500,msService.getMessage("COIN_ILLEGAL"));
        }

        if(StringUtils.isEmpty(address)){
            return error(500,unit + " " + msService.getMessage("WRONG_ADDRESS"));
        }

        Coin coin = coinService.findByUnit(unit);
        if (coin != null) {
            memberWallet = memberWalletService.findByCoinAndAddress(coin, address);
            if (memberWallet != null) {
                payToMemberVo.setBalance(memberWallet.getBalance());
                Member member = memberService.findOne(memberWallet.getMemberId());
                payToMemberVo.setUserName(member == null ? "" : member.getUsername());
                payToMemberVo.setIsAddress(BooleanEnum.IS_TRUE);
                payToMemberVo.setMemberId(memberWallet.getMemberId());
                memberId = memberWallet.getMemberId();
            }
        }

        PayWalletPlatMemberBind platMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(memberId,thirdMark);
        if (platMemberBind == null || platMemberBind.getRoleId() == 1){
            //普通用户
            payToMemberVo.setIsBusiness( BooleanEnum.IS_FALSE);
        }else {
            //商家
            payToMemberVo.setIsBusiness(BooleanEnum.IS_TRUE);
            payToMemberVo.setUserName(platMemberBind.getBusinessName());
        }

        long roleId = platMemberBind == null ? 1L : platMemberBind.getRoleId();
        PayRoleFeeRateConfig feeRateConfig = iPayRoleFeeRateConfigService.findByIdAndTradeUnit(roleId, unit);
        if (feeRateConfig!= null) {
            payToMemberVo.setFeeType(feeRateConfig.getFeeType());
            payToMemberVo.setFeeUnit(feeRateConfig.getFeeUnit());
            payToMemberVo.setFastFee(feeRateConfig.getIncomeFee());
        }else {
            PaySupportCoinConfig coinConfig = iPaySupportCoinConfigService.findByStatusAndUnitAndAppId(
                    unit,BooleanEnum.IS_TRUE.getOrdinal(),thirdMark);
            if (coinConfig != null){
                payToMemberVo.setIsFast(coinConfig.getIsRapidTransfer());
                payToMemberVo.setFastFee(coinConfig.getAssetTransferRapidFee());
                payToMemberVo.setFeeUnit(coinConfig.getUnit());
            }
        }

        return success("查询成功",payToMemberVo);

    }


    @ApiOperation(value = "查询商家名称")
    @PostMapping("businessName")
    public MessageRespResult businessName(@SessionAttribute(SESSION_MEMBER) AuthMember member){
        PayWalletPlatMemberBind payWalletPlatMemberBind = iPayWalletPlatMemberBindService.findByMemberIdAndAppId(member.getId(),member.getPlatform());
        return success("查询成功", payWalletPlatMemberBind);
    }

}
